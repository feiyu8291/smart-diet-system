package com.diet.modules.biz.model.vo;

import com.diet.modules.biz.model.entity.DietFamilyMealPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 联合配餐详情总视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "单餐次就餐详情视图对象")
public class DietMealDetailVO {
    @Schema(description = "是否有该餐次排餐记录")
    private Boolean hasMeal;
    @Schema(description = "对应的排餐计划基本信息")
    private DietFamilyMealPlan mealPlan;
    @Schema(description = "该餐次选择的做法分支菜品列表")
    private List<com.diet.modules.biz.model.entity.DietDishCookingBranch> dishes;
    @Schema(description = "当前家庭各成员的建议进食分量表")
    private List<DietPortionVO> portions;
    @Schema(description = "当前餐次的食材原料清单(带累计总量)")
    private List<DietGroceryVO> groceries;
}
