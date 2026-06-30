package com.diet.modules.biz.model.vo;

import com.diet.modules.biz.model.entity.DietDish;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜品详情视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "菜谱完整详情视图对象")
public class DietDishDetailVO {
    @Schema(description = "做法分支对应的菜品详情")
    private DietDish dish;
    @Schema(description = "原材料列表")
    private List<DietDishIngredientVO> ingredients;
    @Schema(description = "步骤列表")
    private List<DietDishStepVO> steps;
    @Schema(description = "做法分支列表")
    private List<DietDishBranchVO> branches;
}
