package com.diet.modules.biz.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 联合配餐详情查询 PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "日配餐详情查询参数实体类")
public class DietMealDetailQueryPO {
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "排餐目标就餐日期")
    private String targetDate;
    @Schema(description = "排餐餐次")
    private Integer mealPeriod;
}
