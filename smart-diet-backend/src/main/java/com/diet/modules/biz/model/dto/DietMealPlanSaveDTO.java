package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 配餐计划保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "配餐计划单餐保存传输对象")
public class DietMealPlanSaveDTO {
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "排餐目标日期")
    private String targetDate;
    @Schema(description = "餐次 (1-早餐, 2-午餐, 3-晚餐)")
    private Integer mealPeriod;
    @Schema(description = "建议就餐模式 (0-正常饮食, 1-轻食减脂, 2-放纵餐)")
    private Integer dietMode;
    @Schema(description = "做法分支ID列表")
    private List<Long> branchIds;
}
