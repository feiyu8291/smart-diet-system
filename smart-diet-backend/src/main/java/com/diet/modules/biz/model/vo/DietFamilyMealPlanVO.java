package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家庭联合配餐计划视图 VO
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Data
@Schema(description = "联合配餐计划视图对象")
public class DietFamilyMealPlanVO {
    @Schema(description = "排餐计划ID")
    private Long mealPlanId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "就餐日期")
    private LocalDate mealDate;
    @Schema(description = "1-早餐, 2-午餐, 3-晚餐")
    private Integer mealPeriod;
    @Schema(description = "0-正常, 1-轻食, 2-放纵")
    private Integer mealDietMode;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
