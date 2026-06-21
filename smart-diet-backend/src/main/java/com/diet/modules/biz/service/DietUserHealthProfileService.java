package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietUserHealthProfileMapper;
import com.diet.modules.biz.model.dto.DietUserHealthProfileDTO;
import com.diet.modules.biz.model.vo.DietUserHealthProfileVO;
import com.diet.modules.biz.model.entity.DietUserHealthProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成员健康档案服务类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
public class DietUserHealthProfileService extends ServiceImpl<DietUserHealthProfileMapper, DietUserHealthProfile> {

    /**
     * 计算并更新用户健康指标 (BMR, TDEE, DailyTargetCalories)
     */
    public void calculateHealthMetrics(DietUserHealthProfile profile) {
        if (profile == null || profile.getMemberWeight() == null || profile.getMemberHeight() == null
                || profile.getMemberAge() == null || profile.getMemberGender() == null) {
            return;
        }

        double weight = profile.getMemberWeight().doubleValue();
        double height = profile.getMemberHeight().doubleValue();
        int age = profile.getMemberAge();
        int gender = profile.getMemberGender();

        // 1. 计算基础代谢率 (BMR) - Harris-Benedict 公式
        double bmr;
        if (gender == 1) { // 男
            bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else { // 女
            bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        }
        profile.setBmrCalories(BigDecimal.valueOf(bmr).setScale(2, RoundingMode.HALF_UP));

        // 2. 计算每日总能量消耗 (TDEE) - 基于活动强度系数
        double tdeeFactor = 1.2;
        Integer activity = profile.getActivityLevel();
        if (activity != null) {
            switch (activity) {
                case 1:
                    tdeeFactor = 1.2;
                    break;   // 久坐
                case 2:
                    tdeeFactor = 1.375;
                    break; // 轻度活动
                case 3:
                    tdeeFactor = 1.55;
                    break;  // 中度活动
                case 4:
                    tdeeFactor = 1.725;
                    break; // 重度活动
                default:
                    tdeeFactor = 1.2;
            }
        }
        double tdee = bmr * tdeeFactor;
        profile.setTdeeCalories(BigDecimal.valueOf(tdee).setScale(2, RoundingMode.HALF_UP));

        // 3. 计算推荐每日目标摄入热量 (Daily Target Calories)
        double dietSpeedVal = (profile.getDietSpeed() != null) ? profile.getDietSpeed().doubleValue() : 0.5;
        double calorieDeficit = dietSpeedVal * 1100.0;
        double dailyTarget = tdee - calorieDeficit;

        // 设置安全红线：不低于 BMR 的 90%，且绝对不低于 1000 kcal
        double safetyLimit = Math.max(bmr * 0.9, 1000.0);
        if (dailyTarget < safetyLimit) {
            dailyTarget = safetyLimit;
        }
        profile.setDailyTargetCalories(BigDecimal.valueOf(dailyTarget).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 保存或更新就餐成员健康档案，并自动重算指标
     */
    public boolean saveOrUpdateProfile(DietUserHealthProfile profile) {
        if (profile == null) {
            return false;
        }
        // 自动计算指标并落库
        calculateHealthMetrics(profile);
        return saveOrUpdate(profile);
    }

    /**
     * 根据家庭组ID获取所有有效的家庭成员档案
     */
    public List<DietUserHealthProfile> getProfilesByGroupId(Long groupId) {
        LambdaQueryWrapper<DietUserHealthProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietUserHealthProfile::getGroupId, groupId)
                .eq(DietUserHealthProfile::getDelFlag, 0);
        return list(wrapper);
    }

    /**
     * 获取成员档案列表，并转化为 VO 响应
     */
    public List<DietUserHealthProfileVO> listProfiles(Long groupId) {
        List<DietUserHealthProfile> list = this.getProfilesByGroupId(groupId);
        return list.stream().map(entity -> {
            DietUserHealthProfileVO vo = new DietUserHealthProfileVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).toList();
    }

    /**
     * 保存/更新档案（DTO接收，自动重算）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveProfile(DietUserHealthProfileDTO dto) {
        if (dto == null) {
            return false;
        }
        DietUserHealthProfile entity;
        if (dto.getProfileId() != null) {
            entity = this.getById(dto.getProfileId());
            if (entity == null) {
                throw new RuntimeException("档案不存在");
            }
        } else {
            entity = new DietUserHealthProfile();
            entity.setDelFlag(0);
        }

        BeanUtil.copyProperties(dto, entity);
        return this.saveOrUpdateProfile(entity);
    }

    /**
     * 软删除档案
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProfile(Long profileId) {
        DietUserHealthProfile profile = this.getById(profileId);
        if (profile != null) {
            profile.setDelFlag(1); // 软删除
            profile.setUpdateTime(LocalDateTime.now());
            return this.updateById(profile);
        }
        return false;
    }
}
