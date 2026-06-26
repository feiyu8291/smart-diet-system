package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 做法分支展示 VO
 *
 * @author FeiYu
 * @date 2026-06-27
 */
@Data
@Schema(description = "做法分支视图展示对象")
public class DietDishBranchVO {

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

    @Schema(description = "做法分支封面图片访问地址")
    private String previewUrl;

    @Schema(description = "创建者姓名/来源说明")
    private String creatorName = "系统生成";

    @Schema(description = "热量 (kcal/100g)")
    private BigDecimal calories;

    @Schema(description = "蛋白质 (g/100g)")
    private BigDecimal protein;

    @Schema(description = "脂肪 (g/100g)")
    private BigDecimal fat;

    @Schema(description = "碳水 (g/100g)")
    private BigDecimal carbs;

    @Schema(description = "原材料配方列表")
    private List<DietDishIngredientVO> ingredients;

    @Schema(description = "烹饪工序步骤列表")
    private List<DietDishStepVO> steps;

}
