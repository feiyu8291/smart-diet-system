package com.diet.modules.biz.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品配料保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Data
public class DietDishIngredientSaveDTO {
    private Long ingredientId;
    private BigDecimal useAmount;
    private Integer mainMaterialFlag; // 1-主料, 0-辅料/调料
}
