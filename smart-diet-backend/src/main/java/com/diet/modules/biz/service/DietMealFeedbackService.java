package com.diet.modules.biz.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietMealFeedbackMapper;
import com.diet.modules.biz.model.entity.DietFamilyMealPlan;
import com.diet.modules.biz.model.entity.DietMealFeedback;
import com.diet.modules.biz.model.entity.DietUserHealthProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用餐就餐打卡反馈服务类
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Service
@RequiredArgsConstructor
public class DietMealFeedbackService extends ServiceImpl<DietMealFeedbackMapper, DietMealFeedback> {

    private final DietUserHealthProfileService userHealthProfileService;
    private final DietFamilyMealPlanService familyMealPlanService;

    /**
     * 查询指定就餐人成员当天的用餐打卡反馈列表
     *
     * @param profileId 成员健康档案ID
     * @return 今日的用餐反馈列表
     * @author Antigravity
     * @date 2026-06-30
     */
    public List<DietMealFeedback> listTodayFeedback(Long profileId) {
        if (Objects.isNull(profileId)) {
            return Collections.emptyList();
        }

        DietUserHealthProfile profile = userHealthProfileService.getById(profileId);
        if (Objects.isNull(profile) || Objects.isNull(profile.getGroupId())) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        List<DietFamilyMealPlan> plans = familyMealPlanService.lambdaQuery()
                .eq(DietFamilyMealPlan::getGroupId, profile.getGroupId())
                .eq(DietFamilyMealPlan::getMealDate, today)
                .list();

        if (CollUtil.isEmpty(plans)) {
            return Collections.emptyList();
        }

        List<Long> planIds = plans.stream()
                .map(DietFamilyMealPlan::getMealPlanId)
                .collect(Collectors.toList());

        return this.lambdaQuery()
                .eq(DietMealFeedback::getProfileId, profileId)
                .in(DietMealFeedback::getMealPlanId, planIds)
                .list();
    }
}
