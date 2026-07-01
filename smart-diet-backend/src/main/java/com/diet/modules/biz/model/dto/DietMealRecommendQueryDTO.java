package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "智能配餐推荐生成请求传输对象")
public class DietMealRecommendQueryDTO {
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "排餐目标日期(yyyy-MM-dd)")
    private String mealDate;
    @Schema(description = "建议就餐模式(0-正常, 1-轻食, 2-放纵)")
    private Integer dietMode;
}
