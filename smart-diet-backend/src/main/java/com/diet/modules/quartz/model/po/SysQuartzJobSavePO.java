package com.diet.modules.quartz.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务新增PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "定时任务新增PO")
public class SysQuartzJobSavePO {

    @NotBlank(message = "计划名字不能为空")
    @Schema(description = "计划名字")
    private String jobName;

    @Schema(description = "分组 暂未使用")
    private String jobGroup;

    @NotBlank(message = "cron表达式不能为空")
    @Schema(description = "cron表达式")
    private String cronExpression;

    @Schema(description = "描述")
    private String jobDescription;

    @NotBlank(message = "处理类名不能为空")
    @Schema(description = "处理类名(带完整包名的Bean类名)")
    private String handleClass;

    @Schema(description = "方法参数")
    private String methodParam;

    @Schema(description = "任务结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否永久有效 0否1是")
    private Integer permanentState;

    @Schema(description = "任务类型 1：系统任务，2：业务任务")
    private Integer jobType;
}
