package com.diet.modules.biz.model.vo;

import com.diet.modules.common.aspect.DictAnnotation;
import com.diet.modules.common.aspect.DictTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 食材原材料视图 VO
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Data
@Schema(description = "食材原材料视图对象")
public class DietIngredientVO {
    @Schema(description = "原材料ID")
    private Long ingredientId;
    @Schema(description = "原材料名称")
    private String ingredientName;
    @Schema(description = "卡路里 (kcal)")
    private BigDecimal calories;
    @Schema(description = "蛋白质 (g)")
    private BigDecimal protein;
    @Schema(description = "脂肪 (g)")
    private BigDecimal fat;
    @Schema(description = "碳水化合物 (g)")
    private BigDecimal carbs;
    @Schema(description = "计量单位")
    private String measureUnit;
    @Schema(description = "食材类型(1-荤菜类, 2-素菜类, 3-调辅配料, 4-基础调味)")
    @DictAnnotation(type = DictTypeEnum.INGREDIENT_TYPE, target = "ingredientTypeLabel")
    private Integer ingredientType;
    @Schema(description = "食材类型中文名称")
    private String ingredientTypeLabel;
    @Schema(description = "食材描述")
    private String ingredientDesc;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
