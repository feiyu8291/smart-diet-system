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
    @Schema(description = "hasMeal")
    private Boolean hasMeal;
    @Schema(description = "mealPlan")
    private DietFamilyMealPlan mealPlan;
    @Schema(description = "dishes")
    private List<com.diet.modules.biz.model.entity.DietDishCookingBranch> dishes;
    @Schema(description = "portions")
    private List<DietPortionVO> portions;
    @Schema(description = "groceries")
    private List<DietGroceryVO> groceries;
}
