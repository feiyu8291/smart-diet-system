package com.diet.modules.biz.model.dto;

import com.diet.modules.biz.service.DietFamilyMealPlanService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 吃饭完成反馈 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "餐次打卡完成传输对象")
public class DietMealCompleteDTO {
    @Schema(description = "mealPlanId")
    private Long mealPlanId;
    @Schema(description = "dislikes")
    private List<DietFamilyMealPlanService.DislikeFeedback> dislikes;
}
