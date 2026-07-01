package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "单餐成员就餐膳食分量视图对象")
public class DietMemberMealPortionVO {
    @Schema(description = "联合配餐计划ID")
    private Long mealPlanId;
    @Schema(description = "餐次名称，如：早餐 🥞")
    private String periodName;
    @Schema(description = "餐次编码：1-早餐, 2-午餐, 3-晚餐")
    private Integer periodCode;
    @Schema(description = "建议膳食模式名称")
    private String dietModeName;
    @Schema(description = "本餐包含的菜品及分量列表")
    private List<DietMemberDishPortionVO> dishes;
}
