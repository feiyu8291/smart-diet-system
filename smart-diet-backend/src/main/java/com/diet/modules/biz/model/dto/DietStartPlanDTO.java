package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 启动健康膳食模板计划 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "启动配餐计划传输对象")
public class DietStartPlanDTO {
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "计划ID")
    private Long planId;
}
