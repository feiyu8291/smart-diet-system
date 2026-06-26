package com.diet.modules.biz.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 联合配餐推荐查询 PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "智能推荐配餐查询参数实体类")
public class DietMealRecommendQueryPO {
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "targetDate")
    private String targetDate;
    @Schema(description = "mealPeriod")
    private Integer mealPeriod;
    @Schema(description = "建议就餐模式 (0-正常饮食, 1-轻食减脂, 2-放纵餐)")
    private Integer dietMode;
    private Integer limit = 2;
}
