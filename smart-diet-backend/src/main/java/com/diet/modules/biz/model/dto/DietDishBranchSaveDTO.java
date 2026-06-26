package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 做法分支保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-27
 */
@Data
@Schema(description = "做法分支保存传输对象")
public class DietDishBranchSaveDTO {

    @Schema(description = "做法分支ID")
    private Long branchId;

    @Schema(description = "分支做法名称")
    private String branchName;

    @Schema(description = "做法专属菜系类型")
    private String cuisineType;

    @Schema(description = "建议就餐膳食模式 (0-正常饮食, 1-轻食减脂, 2-放纵餐)")
    private Integer dietMode;

    @Schema(description = "做法分支封面图片ID")
    private Long coverImageId;

    @Schema(description = "是否自动核算营养素(根据原料重量比例自动折算)")
    private Boolean autoCalculateNutrients;

    @Schema(description = "热量 (kcal/100g)")
    private BigDecimal calories;

    @Schema(description = "蛋白质 (g/100g)")
    private BigDecimal protein;

    @Schema(description = "脂肪 (g/100g)")
    private BigDecimal fat;

    @Schema(description = "碳水化合物 (g/100g)")
    private BigDecimal carbs;

    @Schema(description = "该做法关联的原材料配比列表")
    private List<DietDishIngredientSaveDTO> ingredients;

    @Schema(description = "该做法关联的烹饪操作步骤列表")
    private List<DietDishStepSaveDTO> steps;

}
