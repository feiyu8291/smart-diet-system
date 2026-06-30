package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietCookingStepPoolMapper;
import com.diet.modules.biz.model.entity.DietCookingStepPool;
import com.diet.modules.biz.model.po.DietCookingStepQueryPO;
import com.diet.modules.biz.model.vo.DietCookingStepPoolVO;
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
 * 烹饪标准步骤池业务服务类
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Slf4j
@Service
public class DietCookingStepPoolService extends ServiceImpl<DietCookingStepPoolMapper, DietCookingStepPool> {

    /**
     * 获取标准步骤模板列表
     *
     * @param name 步骤模板名称模糊匹配
     * @return 步骤池VO列表
     */
    public List<DietCookingStepPoolVO> listSteps(String name) {
        List<DietCookingStepPool> list = this.lambdaQuery()
                .like(CharSequenceUtil.isNotBlank(name), DietCookingStepPool::getStepName, CharSequenceUtil.trim(name))
                .orderByAsc(DietCookingStepPool::getStepPoolId)
                .list();
        return list.stream().map(entity -> {
            DietCookingStepPoolVO vo = new DietCookingStepPoolVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 分页查询步骤模板
     *
     * @param po 分页查询条件 PO
     * @return 步骤池VO分页实体
     */
    public IPage<DietCookingStepPoolVO> pageSteps(DietCookingStepQueryPO po) {
        if (Objects.isNull(po)) {
            po = new DietCookingStepQueryPO();
        }
        String name = po.getName();
        IPage<DietCookingStepPool> pageResult = this.lambdaQuery()
                .like(CharSequenceUtil.isNotBlank(name), DietCookingStepPool::getStepName, CharSequenceUtil.trim(name))
                .orderByAsc(DietCookingStepPool::getStepPoolId)
                .page(new Page<>(po.getPageNo(), po.getPageSize()));
        IPage<DietCookingStepPoolVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<DietCookingStepPoolVO> voRecords = pageResult.getRecords().stream().map(entity -> {
            DietCookingStepPoolVO vo = new DietCookingStepPoolVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voRecords);
        return voPage;
    }

    /**
     * 保存或更新步骤模板
     *
     * @param entity 步骤模板实体
     * @return 保存是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveStep(DietCookingStepPool entity) {
        if (Objects.isNull(entity)) {
            return false;
        }
        if (Objects.nonNull(entity.getStepPoolId())) {
            entity.setUpdateTime(LocalDateTime.now());
        } else {
            entity.setDelFlag(0);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
        }
        return saveOrUpdate(entity);
    }

    /**
     * 批量或单条软删除步骤模板
     *
     * @param dto 批量删除入参 DTO
     * @return 删除是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSteps(BaseDeleteDTO dto) {
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

