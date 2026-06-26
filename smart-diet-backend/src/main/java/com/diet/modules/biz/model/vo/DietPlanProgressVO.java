package com.diet.modules.biz.model.vo;

import com.diet.modules.biz.model.entity.DietFamilyPlanProgress;
import com.diet.modules.biz.model.entity.DietPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 计划进度视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "配餐计划进度跟踪视图对象")
public class DietPlanProgressVO {
    @Schema(description = "hasActivePlan")
    private Boolean hasActivePlan;
    @Schema(description = "progress")
    private DietFamilyPlanProgress progress;
    @Schema(description = "template")
    private DietPlan template;
}
