package com.diet.modules.biz.model.vo;

import com.diet.modules.biz.model.entity.DietDish;
import com.diet.modules.biz.model.entity.DietFamilyMealPlan;
import lombok.Data;

import java.util.List;

/**
 * 联合配餐详情总视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietMealDetailVO {
    private Boolean hasMeal;
    private DietFamilyMealPlan mealPlan;
    private List<DietDish> dishes;
    private List<DietPortionVO> portions;
    private List<DietGroceryVO> groceries;
}
