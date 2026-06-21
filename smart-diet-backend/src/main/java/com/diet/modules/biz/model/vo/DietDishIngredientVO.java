package com.diet.modules.biz.model.vo;

import lombok.Data;

/**
 * 菜品配料视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietDishIngredientVO {
    private Long relationId;
    private String useAmount;
    private Integer mainMaterialFlag;
    private Long ingredientId;
    private String ingredientName;
    private String measureUnit;
}
