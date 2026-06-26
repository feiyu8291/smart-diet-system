package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜谱完整信息保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Data
@Schema(description = "菜谱完整信息保存传输对象")
public class DietDishSaveDTO {
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "菜谱名称")
    private String dishName;
    @Schema(description = "封面图片ID")
    private Long coverImageId;

    @Schema(description = "原材料列表")
    private List<DietDishIngredientSaveDTO> ingredients;
    @Schema(description = "步骤列表")
    private List<DietDishStepSaveDTO> steps;
    @Schema(description = "做法分支列表")
    private List<DietDishBranchSaveDTO> branches;
}
