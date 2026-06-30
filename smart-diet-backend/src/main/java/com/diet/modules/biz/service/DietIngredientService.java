package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietIngredientMapper;
import com.diet.modules.biz.model.entity.DietIngredient;
import com.diet.modules.biz.model.po.DietIngredientQueryPO;
import com.diet.modules.biz.model.vo.DietIngredientVO;
import com.diet.modules.common.aspect.DictData;
import com.diet.modules.common.entity.BaseDeleteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 原材料/食材配料业务服务类
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Slf4j
@Service
public class DietIngredientService extends ServiceImpl<DietIngredientMapper, DietIngredient> {

    /**
     * 查询原材料列表 (带字典自动翻译)
     */
    @DictData
    public List<DietIngredientVO> listIngredients(String name, Integer ingredientType) {
        List<DietIngredient> list = this.lambdaQuery()
                .like(CharSequenceUtil.isNotBlank(name), DietIngredient::getIngredientName, CharSequenceUtil.trim(name))
                .eq(Objects.nonNull(ingredientType), DietIngredient::getIngredientType, ingredientType)
                .orderByAsc(DietIngredient::getIngredientId)
                .list();
        return list.stream().map(entity -> {
            DietIngredientVO vo = new DietIngredientVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 原材料分页查询 (带字典自动翻译)
     */
    @DictData
    public IPage<DietIngredientVO> pageIngredients(DietIngredientQueryPO po) {
        if (Objects.isNull(po)) {
            po = new DietIngredientQueryPO();
        }
        String name = po.getName();
        IPage<DietIngredient> pageResult = this.lambdaQuery()
                .like(CharSequenceUtil.isNotBlank(name), DietIngredient::getIngredientName, CharSequenceUtil.trim(name))
                .eq(Objects.nonNull(po.getIngredientType()), DietIngredient::getIngredientType, po.getIngredientType())
                .orderByAsc(DietIngredient::getIngredientId)
                .page(new Page<>(po.getPageNo(), po.getPageSize()));
        IPage<DietIngredientVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<DietIngredientVO> voRecords = pageResult.getRecords().stream().map(entity -> {
            DietIngredientVO vo = new DietIngredientVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voRecords);
        return voPage;
    }

    /**
     * 保存或更新原材料
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveIngredient(DietIngredient entity) {
        if (Objects.isNull(entity)) {
            return false;
        }
        if (Objects.nonNull(entity.getIngredientId())) {
            entity.setUpdateTime(LocalDateTime.now());
        } else {
            entity.setDelFlag(0);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
        }
        return saveOrUpdate(entity);
    }

    /**
     * 批量或单条软删除原材料
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteIngredients(BaseDeleteDTO dto) {
        if (Objects.isNull(dto)) {
            return false;
        }
        Set<Long> ids = dto.allIds();
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        return this.removeBatchByIds(ids);
    }
}


