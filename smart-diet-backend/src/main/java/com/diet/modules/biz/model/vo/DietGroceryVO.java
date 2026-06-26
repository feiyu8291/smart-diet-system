package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 食材采购清单视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "采购清单条目视图对象")
public class DietGroceryVO {
    @Schema(description = "原材料ID")
    private Long ingredientId;
    @Schema(description = "useAmount")
    private BigDecimal useAmount;
    @Schema(description = "原材料名称")
    private String ingredientName;
    @Schema(description = "measureUnit")
    private String measureUnit;
    @Schema(description = "食材类型(1-荤菜类, 2-素菜类, 3-调辅配料, 4-基础调味)")
    private Integer ingredientType;
    @Schema(description = "食材描述(健康选购及烹饪备注)")
    private String ingredientDesc;
}
