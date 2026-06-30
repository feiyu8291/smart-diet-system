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
    @Schema(description = "当前是否有执行中的膳食计划")
    private Boolean hasActivePlan;
    @Schema(description = "计划执行进度百分比 (0-100)")
    private DietFamilyPlanProgress progress;
    @Schema(description = "关联的膳食计划模板详情")
    private DietPlan template;
}
