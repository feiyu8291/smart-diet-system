package com.diet.modules.biz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietFamilyGroupMapper;
import com.diet.modules.biz.model.entity.DietFamilyGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    public List<DietFamilyGroup> listGroups(Long userId) {
        LambdaQueryWrapper<DietFamilyGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietFamilyGroup::getCreatorUserId, userId)
                .eq(DietFamilyGroup::getDelFlag, 0)
                .orderByDesc(DietFamilyGroup::getCreateTime);
        return list(wrapper);
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
     * 软删除家庭分组
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteGroup(Long groupId) {
        DietFamilyGroup entity = getById(groupId);
        if (entity != null) {
            entity.setDelFlag(1);
            entity.setUpdateTime(LocalDateTime.now());
            return updateById(entity);
        }
        return false;
    }
}
