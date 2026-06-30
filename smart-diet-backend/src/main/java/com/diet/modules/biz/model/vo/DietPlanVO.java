package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 计划模板视图 VO
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Data
@Schema(description = "计划模板视图对象")
public class DietPlanVO {
    @Schema(description = "计划ID")
    private Long planId;
    @Schema(description = "计划名称")
    private String planName;
    @Schema(description = "总天数")
    private Integer totalDays;
    @Schema(description = "计划具体阶段指引描述")
    private String planDescription;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
