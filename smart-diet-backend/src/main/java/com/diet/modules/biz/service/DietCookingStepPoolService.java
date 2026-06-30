package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietCookingStepPoolMapper;
import com.diet.modules.biz.model.entity.DietCookingStepPool;
import com.diet.modules.biz.model.po.DietCookingStepQueryPO;
import com.diet.modules.biz.model.vo.DietCookingStepPoolVO;
import com.diet.modules.common.entity.BaseDeleteDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 烹饪标准步骤池业务服务类
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Service
public class DietCookingStepPoolService extends ServiceImpl<DietCookingStepPoolMapper, DietCookingStepPool> {

    /**
     * 获取标准步骤模板列表
     */
    public List<DietCookingStepPoolVO> listSteps(String name) {
        List<DietCookingStepPool> list = this.lambdaQuery()
                .eq(DietCookingStepPool::getDelFlag, 0)
                .like(name != null && !name.trim().isEmpty(), DietCookingStepPool::getStepName, name != null ? name.trim() : null)
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
     */
    public IPage<DietCookingStepPoolVO> pageSteps(DietCookingStepQueryPO po) {
        if (po == null) {
            po = new DietCookingStepQueryPO();
        }
        String name = po.getName();
        IPage<DietCookingStepPool> pageResult = this.lambdaQuery()
                .eq(DietCookingStepPool::getDelFlag, 0)
                .like(name != null && !name.trim().isEmpty(), DietCookingStepPool::getStepName, name != null ? name.trim() : null)
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
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveStep(DietCookingStepPool entity) {
        if (entity == null) {
            return false;
        }
        if (entity.getStepPoolId() != null) {
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
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSteps(BaseDeleteDTO dto) {
        if (dto == null) {
            return false;
        }
        Set<Long> ids = dto.allIds();
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        List<DietCookingStepPool> list = ids.stream().map(id -> {
            DietCookingStepPool entity = new DietCookingStepPool();
            entity.setStepPoolId(id);
            entity.setDelFlag(1); // 软删除
            entity.setUpdateTime(LocalDateTime.now());
            return entity;
        }).collect(Collectors.toList());

        return updateBatchById(list);
    }
}

