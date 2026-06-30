package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜品配料视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "菜谱关联原材料详情视图对象")
public class DietDishIngredientVO {
    @Schema(description = "关联主键ID")
    private Long relationId;
    @Schema(description = "每100g成品消耗原料量(g)")
    private String useAmount;
    @Schema(description = "是否为主料 (1-主料, 2-辅料, 3-调料)")
    private Integer mainMaterialFlag;
    @Schema(description = "原材料ID")
    private Long ingredientId;
    @Schema(description = "原材料名称")
    private String ingredientName;
    @Schema(description = "原料计量单位")
    private String measureUnit;
}
