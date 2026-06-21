package com.diet.modules.quartz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 自动任务参数结构
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Accessors(chain = true)
@Schema(description = "自动任务实体传输对象")
public class QuartzJobEntityDTO implements Serializable {

    @Schema(description = "主键ID")
    private Long jobId;

    @Schema(description = "任务计划名称")
    private String jobName;

    @Schema(description = "任务分组（暂未使用）")
    private String jobGroup;

    @Schema(description = "cron表达式")
    private String cronExpression;

    @Schema(description = "任务描述")
    private String jobDescription;

    @Schema(description = "Spring Bean名称(如: demoQuartzJobHandler)")
    private String handleClass;

    @Schema(description = "方法参数，字符串格式按业务解析")
    private String methodParam;

    @Schema(description = "是否停用 (0:否, 1:是)")
    private Integer delFlag;

    @Schema(description = "任务结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否永久有效 (0:否, 1:是)")
    private Integer permanentState;

    @Schema(description = "任务类型 (1:系统任务, 2:业务任务)")
    private Integer jobType;

    @Schema(description = "任务创建记录时间")
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}
