package com.diet.modules.biz.model.vo;

import com.diet.modules.biz.model.entity.DietFamilyPlanProgress;
import com.diet.modules.biz.model.entity.DietPlan;
import lombok.Data;

/**
 * 计划进度视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietPlanProgressVO {
    private Boolean hasActivePlan;
    private DietFamilyPlanProgress progress;
    private DietPlan template;
}
