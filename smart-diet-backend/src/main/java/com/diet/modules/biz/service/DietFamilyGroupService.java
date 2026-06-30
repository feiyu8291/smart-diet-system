package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietFamilyGroupMapper;
import com.diet.modules.biz.model.entity.DietFamilyGroup;
import com.diet.modules.biz.model.po.DietFamilyGroupQueryPO;
import com.diet.modules.biz.model.vo.DietFamilyGroupVO;
import com.diet.modules.common.entity.BaseDeleteDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 家庭分组服务类
 *
 * @author FeiYu
 * @date 2026-06-21
 */
@Service
public class DietFamilyGroupService extends ServiceImpl<DietFamilyGroupMapper, DietFamilyGroup> {

    /**
     * 根据创建人用户 ID 获取所有有效的家庭分组列表
     */
    public List<DietFamilyGroupVO> listGroups(Long userId) {
        List<DietFamilyGroup> list = this.lambdaQuery()
                .eq(userId != null, DietFamilyGroup::getCreatorUserId, userId)
                .eq(DietFamilyGroup::getDelFlag, 0)
                .orderByDesc(DietFamilyGroup::getCreateTime)
                .list();
        return list.stream().map(entity -> {
            DietFamilyGroupVO vo = new DietFamilyGroupVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 保存或修改家庭分组
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveGroup(DietFamilyGroup entity) {
        if (entity == null) {
            return false;
        }
        if (entity.getGroupId() == null) {
            entity.setDelFlag(0);
            entity.setCreateTime(LocalDateTime.now());
            if (entity.getCooldownDays() == null) {
                entity.setCooldownDays(3); // 默认冷却天数为 3 天
            }
        }
        entity.setUpdateTime(LocalDateTime.now());
        return saveOrUpdate(entity);
    }

    /**
     * 批量或单条软删除家庭分组
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteGroups(BaseDeleteDTO dto) {
        if (dto == null) {
            return false;
        }
        Set<Long> ids = dto.allIds();
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        List<DietFamilyGroup> list = ids.stream().map(id -> {
            DietFamilyGroup entity = new DietFamilyGroup();
            entity.setGroupId(id);
            entity.setDelFlag(1); // 软删除
            entity.setUpdateTime(LocalDateTime.now());
            return entity;
        }).collect(Collectors.toList());

        return updateBatchById(list);
    }

    /**
     * 分页获取家庭组列表
     */
    public IPage<DietFamilyGroupVO> pageGroups(DietFamilyGroupQueryPO po) {
        if (po == null) {
            po = new DietFamilyGroupQueryPO();
        }
        String groupName = po.getGroupName();
        IPage<DietFamilyGroup> pageResult = this.lambdaQuery()
                .eq(DietFamilyGroup::getDelFlag, 0)
                .like(groupName != null && !groupName.trim().isEmpty(), DietFamilyGroup::getGroupName, groupName != null ? groupName.trim() : null)
                .eq(po.getCreatorUserId() != null, DietFamilyGroup::getCreatorUserId, po.getCreatorUserId())
                .orderByDesc(DietFamilyGroup::getCreateTime)
                .page(new Page<>(po.getPageNo(), po.getPageSize()));
        IPage<DietFamilyGroupVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<DietFamilyGroupVO> voRecords = pageResult.getRecords().stream().map(entity -> {
            DietFamilyGroupVO vo = new DietFamilyGroupVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voRecords);
        return voPage;
    }
}

