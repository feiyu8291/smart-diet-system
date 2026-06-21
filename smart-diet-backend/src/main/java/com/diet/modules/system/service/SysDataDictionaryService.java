package com.diet.modules.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.common.constant.CacheKeyConstant;
import com.diet.modules.common.exception.BusinessException;
import com.diet.modules.common.util.RedisUtil;
import com.diet.modules.system.mapper.SysDataDictionaryMapper;
import com.diet.modules.system.model.dto.SysDataDictionaryDTO;
import com.diet.modules.system.model.entity.SysDataDictionary;
import com.diet.modules.system.model.po.SysDataDictionaryQueryPO;
import com.diet.modules.system.model.vo.SysDataDictPageVO;
import com.diet.modules.system.model.vo.SysDataDictTypeVO;
import com.diet.modules.system.model.vo.SysDataDictVO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据字典 Service
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Service
public class SysDataDictionaryService extends ServiceImpl<SysDataDictionaryMapper, SysDataDictionary> implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        this.clearDictCache();
        log.info("系统启动完毕，已自动清理数据字典 Redis 缓存");
    }

    public Page<SysDataDictPageVO> page(SysDataDictionaryQueryPO queryPO) {
        Page<SysDataDictionary> entityPage = this.lambdaQuery()
                .like(StringUtils.hasText(queryPO.getDataType()), SysDataDictionary::getDataType, queryPO.getDataType())
                .like(StringUtils.hasText(queryPO.getDataTypeName()), SysDataDictionary::getDataTypeName, queryPO.getDataTypeName())
                .like(StringUtils.hasText(queryPO.getDataCode()), SysDataDictionary::getDataCode, queryPO.getDataCode())
                .like(StringUtils.hasText(queryPO.getDataValue()), SysDataDictionary::getDataValue, queryPO.getDataValue())
                .orderByAsc(SysDataDictionary::getDataType)
                .orderByAsc(SysDataDictionary::getDictSort)
                .page(new Page<>(queryPO.getPageNo(), queryPO.getPageSize()));

        Page<SysDataDictPageVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        if (CollUtil.isNotEmpty(entityPage.getRecords())) {
            List<SysDataDictPageVO> voList = entityPage.getRecords().stream().map(entity -> {
                SysDataDictPageVO vo = new SysDataDictPageVO();
                vo.setDictId(entity.getDictId());
                vo.setDataTypeName(entity.getDataTypeName());
                vo.setDataType(entity.getDataType());
                vo.setDataCode(entity.getDataCode());
                vo.setDataValue(entity.getDataValue());
                vo.setParentType(entity.getParentType());
                vo.setParentCode(entity.getParentCode());
                vo.setDataRemark(entity.getDataRemark());
                vo.setWebReadOnly(entity.getWebReadOnly());
                vo.setDefaultState(entity.getDefaultState());
                vo.setDictSort(entity.getDictSort());
                vo.setInitSystemFlag(entity.getInitSystemFlag());
                return vo;
            }).collect(Collectors.toList());
            voPage.setRecords(voList);
        }
        return voPage;
    }

    public List<SysDataDictionary> listByType(String dataType) {
        return this.lambdaQuery().eq(SysDataDictionary::getDataType, dataType).orderByAsc(SysDataDictionary::getDataCode).list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateDict(SysDataDictionaryDTO dto) {
        Long dictId = dto.getDictId();
        boolean isUpdate = Objects.nonNull(dictId);

        // 1. 唯一性校验
        this.checkDictUnique(dto.getDataType(), dto.getDataCode(), dictId, isUpdate);

        // 2. 初始化更新对象
        SysDataDictionary entity = this.initDict(dictId, isUpdate);

        // 3. 属性拷贝
        cn.hutool.core.bean.BeanUtil.copyProperties(dto, entity);

        // 4. 保存或更新
        saveOrUpdate(entity);
        this.clearDictCache();
    }

    private void checkDictUnique(String dataType, String dataCode, Long dictId, boolean isUpdate) {
        boolean exists = lambdaQuery()
                .eq(SysDataDictionary::getDataType, dataType)
                .eq(SysDataDictionary::getDataCode, dataCode)
                .ne(isUpdate, SysDataDictionary::getDictId, dictId)
                .exists();
        if (exists) {
            throw BusinessException.withMessage("同类型下相同编码的字典已存在");
        }
    }

    private SysDataDictionary initDict(Long dictId, boolean isUpdate) {
        SysDataDictionary entity = isUpdate ? getById(dictId) : new SysDataDictionary();
        if (isUpdate && Objects.isNull(entity)) {
            throw BusinessException.withMessage("字典记录不存在");
        }
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Collection<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            throw BusinessException.withMessageParamsError("ids 不能为空");
        }
        List<SysDataDictionary> dictList = this.listByIds(ids);
        if (CollUtil.isEmpty(dictList)) {
            throw BusinessException.withMessage("字典记录不存在");
        }
        for (SysDataDictionary dict : dictList) {
            if (dict.getInitSystemFlag() == 1) {
                throw BusinessException.withMessage("系统初始化字典记录不能删除");
            }
        }
        this.removeByIds(ids);
        this.clearDictCache();
    }

    private void clearDictCache() {
        RedisUtil.del(CacheKeyConstant.DICT_ALL);
        log.info("数据字典缓存已清除，key: {}", CacheKeyConstant.DICT_ALL);
    }

    public List<SysDataDictVO> selectByAll() {
        String cacheKey = CacheKeyConstant.DICT_ALL;
        List<SysDataDictVO> dictList = RedisUtil.getCacheList(cacheKey, SysDataDictVO.class);
        if (Objects.nonNull(dictList) && !dictList.isEmpty()) {
            log.info("缓存命中！直接返回 Redis 中的数据");
            return dictList;
        }
        log.info("缓存未命中！执行查库操作！");
        List<SysDataDictionary> dataDictionaryList = this.lambdaQuery().list();
        List<SysDataDictVO> dbDictList = this.convertViewObj(dataDictionaryList);
        if (CollUtil.isNotEmpty(dbDictList)) {
            long expireSeconds = CacheKeyConstant.REDIS_KEY_EXPIRE_12_HOUR;
            RedisUtil.set(cacheKey, dbDictList, expireSeconds);
            log.info("查库数据已存入 Redis 缓存，缓存 key：{}，过期时间：{} 秒", cacheKey, expireSeconds);
        }
        return dbDictList;
    }

    private List<SysDataDictVO> convertViewObj(List<SysDataDictionary> dataDictionaryList) {
        List<SysDataDictVO> dictionaryVOS = new ArrayList<>();
        if (CollUtil.isEmpty(dataDictionaryList)) {
            return dictionaryVOS;
        }
        dataDictionaryList.forEach(dictionary -> {
            SysDataDictVO vo = new SysDataDictVO();
            vo.setDictId(dictionary.getDictId());
            vo.setDataTypeName(dictionary.getDataTypeName());
            vo.setDataType(dictionary.getDataType());
            vo.setDataCode(dictionary.getDataCode());
            vo.setDataValue(dictionary.getDataValue());
            vo.setDataRemark(dictionary.getDataRemark());
            vo.setParentCode(dictionary.getParentCode());
            vo.setParentType(dictionary.getParentType());
            vo.setWebReadOnly(dictionary.getWebReadOnly());
            vo.setDelFlag(dictionary.getDelFlag());
            vo.setDefaultState(dictionary.getDefaultState());
            dictionaryVOS.add(vo);
        });
        return dictionaryVOS;
    }

    public List<SysDataDictVO> selectByType(@NonNull String dataType) {
        if (CharSequenceUtil.isBlank(dataType)) {
            throw BusinessException.withMessageParamsError("查询参数【类型】不能为空");
        }
        List<SysDataDictVO> dictAllList = this.selectByAll();
        return dictAllList.stream().filter(item -> dataType.equals(item.getDataType())).collect(Collectors.toList());
    }

    public SysDataDictVO selectByTypeAndCode(String dataType, String dataCode) {
        if (CharSequenceUtil.isBlank(dataType)) {
            throw BusinessException.withMessageParamsError("查询参数【类型】不能为空");
        }
        Map<String, SysDataDictVO> dictTypeMap = getDictMapByDataType(dataType);
        return dictTypeMap.get(dataCode);
    }

    public String selectValueByTypeAndCode(@NonNull String dataType, @NonNull String dataCode) {
        SysDataDictVO dictionaryVO = this.selectByTypeAndCode(dataType, dataCode);
        if (Objects.isNull(dictionaryVO)) {
            return "";
        }
        return dictionaryVO.getDataValue();
    }

    public Map<String, String> getDictMapByType(@NonNull String dictType) {
        List<SysDataDictVO> dictSingleList = this.selectByType(dictType);
        if (CollUtil.isEmpty(dictSingleList)) {
            return new HashMap<>();
        }
        return dictSingleList.stream().collect(Collectors.toMap(SysDataDictVO::getDataCode, SysDataDictVO::getDataValue, (key1, key2) -> key1));
    }

    public List<SysDataDictVO> getTreeByType(String dataType) {
        List<SysDataDictVO> dictAllList = this.selectByAll();
        Map<String, List<SysDataDictVO>> voMap = dictAllList.stream().collect(Collectors.groupingBy(SysDataDictVO::getDataType));
        Map<String, List<SysDataDictVO>> voOneMap = dictAllList.stream()
                .collect(Collectors.groupingBy(vo -> (vo.getParentType() + vo.getParentCode())));
        for (SysDataDictVO dictionaryVO : dictAllList) {
            List<SysDataDictVO> childList = voOneMap.get(dictionaryVO.getDataType() + dictionaryVO.getDataCode());
            dictionaryVO.setChildList(childList);
        }
        return voMap.get(dataType);
    }

    public List<SysDataDictVO> getDictListByDataTypes(Collection<String> dataTypes) {
        List<SysDataDictVO> dictAllList = this.selectByAll();
        if (Objects.isNull(dataTypes) || dataTypes.isEmpty()) {
            return dictAllList;
        }
        return dictAllList.stream().filter(item -> dataTypes.contains(item.getDataType())).toList();
    }

    public Map<String, List<SysDataDictVO>> getDictMapByDataTypes(Collection<String> dataTypes) {
        List<SysDataDictVO> dictList = this.getDictListByDataTypes(dataTypes);
        return dictList.stream().collect(Collectors.groupingBy(SysDataDictVO::getDataType));
    }

    public Map<String, Map<String, String>> getDictMapCodeValueByDataTypes(Collection<String> dataTypes) {
        return getDictMapByDataTypesInternal(dataTypes, SysDataDictVO::getDataCode, SysDataDictVO::getDataValue);
    }

    public Map<String, Map<String, String>> getDictMapValueCodeByDataTypes(Collection<String> dataTypes) {
        return getDictMapByDataTypesInternal(dataTypes, SysDataDictVO::getDataValue, SysDataDictVO::getDataCode);
    }

    private Map<String, Map<String, String>> getDictMapByDataTypesInternal(Collection<String> dataTypes, Function<SysDataDictVO, String> keyMapper,
                                                                           Function<SysDataDictVO, String> valueMapper) {
        List<SysDataDictVO> dictList = this.getDictListByDataTypes(dataTypes);
        Map<String, Map<String, String>> resultMap = dictList.stream().collect(Collectors.groupingBy(SysDataDictVO::getDataType,
                Collectors.toMap(keyMapper, valueMapper, (oldValue, newValue) -> newValue)));
        log.info("成功获取字典映射：dataTypes={}，映射数量={}", dataTypes, resultMap.size());
        return resultMap;
    }

    public Map<String, SysDataDictVO> getDictMapByDataType(@NonNull String dataType) {
        if (CharSequenceUtil.isBlank(dataType)) {
            return new HashMap<>();
        }
        List<SysDataDictVO> dictAllList = this.selectByAll();
        return dictAllList.stream().filter(item -> dataType.equals(item.getDataType())).collect(Collectors.toMap(SysDataDictVO::getDataCode,
                item -> item, (existing, replacement) -> existing));
    }

    public Map<String, String> getDictCodeValueMapByDataType(@NonNull String dataType) {
        if (CharSequenceUtil.isBlank(dataType)) {
            return new HashMap<>();
        }
        List<SysDataDictVO> dictAllList = this.selectByAll();
        return dictAllList.stream().filter(item -> dataType.equals(item.getDataType()))
                .collect(Collectors.toMap(SysDataDictVO::getDataCode, SysDataDictVO::getDataValue, (existing, replacement) -> existing));
    }

    public List<SysDataDictVO> getChildListByParentTypeAndCode(@NonNull String parentType, @NonNull String parentCode) {
        List<SysDataDictVO> dictAllList = this.selectByAll();
        if (CollUtil.isEmpty(dictAllList)) {
            return dictAllList;
        }
        return dictAllList.stream()
                .filter(dic -> CharSequenceUtil.equals(dic.getParentType(), parentType) && CharSequenceUtil.equals(dic.getParentCode(), parentCode))
                .toList();
    }

    public List<SysDataDictTypeVO> getDataTypeAll() {
        List<SysDataDictVO> list = this.selectByAll();
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        Map<String, String> dictTypeMap = list.stream().collect(Collectors.toMap(
                SysDataDictVO::getDataTypeName,
                SysDataDictVO::getDataType,
                (existing, replacement) -> existing));
        List<SysDataDictTypeVO> dictTypeList = new ArrayList<>();
        for (Map.Entry<String, String> entry : dictTypeMap.entrySet()) {
            SysDataDictTypeVO dictTypeVO = new SysDataDictTypeVO();
            dictTypeVO.setDataTypeName(entry.getKey());
            dictTypeVO.setDataType(entry.getValue());
            dictTypeList.add(dictTypeVO);
        }
        return dictTypeList.stream().sorted(Comparator.comparing(SysDataDictTypeVO::getDataTypeName)).toList();
    }

    public void refreshCache() {
        String cacheKey = CacheKeyConstant.DICT_ALL;
        RedisUtil.del(cacheKey);
        this.selectByAll();
    }

    public List<SysDataDictVO> getFirstLevelDicts() {
        List<SysDataDictVO> list = this.selectByAll();
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return list.stream()
                .filter(item -> CharSequenceUtil.isBlank(item.getParentType()) && CharSequenceUtil.isBlank(item.getParentCode()))
                .collect(Collectors.toList());
    }
}
