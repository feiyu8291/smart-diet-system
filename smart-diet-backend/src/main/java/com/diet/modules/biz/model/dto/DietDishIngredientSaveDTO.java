package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品配料保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Data
@Schema(description = "菜谱原材料关联保存传输对象")
public class DietDishIngredientSaveDTO {
    @Schema(description = "原材料ID")
    private Long ingredientId;
    @Schema(description = "消耗原料量 (g)")
    private BigDecimal useAmount;
    @Schema(description = "1-主料, 0-辅料/调料")
    private Integer mainMaterialFlag;
}
