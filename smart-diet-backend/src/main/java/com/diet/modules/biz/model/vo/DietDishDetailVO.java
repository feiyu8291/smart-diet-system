package com.diet.modules.biz.model.vo;

import com.diet.modules.biz.model.entity.DietDish;
import lombok.Data;

import java.util.List;

/**
 * 菜品详情视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietDishDetailVO {
    private DietDish dish;
    private List<DietDishIngredientVO> ingredients;
    private List<DietDishStepVO> steps;
}
