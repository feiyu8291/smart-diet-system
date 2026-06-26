package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 一整天包含三餐与合并采购清单的数据传输视图对象
 *
 * @author FeiYu
 * @date 2026-06-21
 */
@Data
@Schema(description = "日配餐整体详情视图对象")
public class DietDayMealDetailVO {
    @Schema(description = "本日是否有配餐")
    private Boolean hasMeal;

    @Schema(description = "早餐详情")
    private DietMealDetailVO breakfast;

    @Schema(description = "午餐详情")
    private DietMealDetailVO lunch;

    @Schema(description = "晚餐详情")
    private DietMealDetailVO dinner;

    @Schema(description = "合并后的全天食材采购总清单")
    private List<DietGroceryVO> dailyGroceries;
}
