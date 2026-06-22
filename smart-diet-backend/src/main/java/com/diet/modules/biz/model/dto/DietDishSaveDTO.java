package com.diet.modules.biz.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜谱完整信息保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Data
public class DietDishSaveDTO {
    private Long dishId;
    private String dishName;
    private String cuisineType;
    private Integer dietMode; // 建议就餐模式 (0-正常, 1-轻食, 2-放纵)
    private BigDecimal calories; // 选填，若为null或0则由后端按原材料比例重算
    private BigDecimal protein;  // 选填
    private BigDecimal fat;      // 选填
    private BigDecimal carbs;    // 选填
    private Long coverImageId;   // 封面图片ID
    private String imageIds;     // 成品图片ID列表 (最多3张，逗号分隔)

    private List<DietDishIngredientSaveDTO> ingredients;
    private List<DietDishStepSaveDTO> steps;
}
