package com.diet.modules.biz.model.dto;

import com.diet.modules.biz.service.DietFamilyMealPlanService;
import lombok.Data;

import java.util.List;

/**
 * 吃饭完成反馈 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietMealCompleteDTO {
    private Long mealPlanId;
    private List<DietFamilyMealPlanService.DislikeFeedback> dislikes;
}
