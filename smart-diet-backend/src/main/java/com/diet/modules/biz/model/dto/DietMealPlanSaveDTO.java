package com.diet.modules.biz.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 配餐计划保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietMealPlanSaveDTO {
    private Long groupId;
    private String targetDate;
    private Integer mealPeriod;
    private Integer dietMode;
    private List<Long> dishIds;
}
