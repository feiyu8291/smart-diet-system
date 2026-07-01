package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "智能配餐全天食谱发布确认传输对象")
public class DietMealPublishDTO {
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "排餐目标日期(yyyy-MM-dd)")
    private String mealDate;
    @Schema(description = "全天三餐做法分支推荐详情列表Map")
    private Map<String, List<Map<String, Object>>> menuData;
}
