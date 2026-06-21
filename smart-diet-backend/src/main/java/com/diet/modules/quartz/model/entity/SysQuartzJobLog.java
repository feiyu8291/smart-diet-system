package com.diet.modules.quartz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务日志记录实体类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@TableName("sys_quartz_job_log")
@Schema(description = "定时任务日志实体")
public class SysQuartzJobLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "job_log_id", type = IdType.AUTO)
    private Long jobLogId;

    @Schema(description = "计划ID")
    @TableField(value = "job_id")
    private Long jobId;

    @Schema(description = "cron表达式")
    @TableField(value = "cron_expression")
    private String cronExpression;

    @Schema(description = "执行描述/结果")
    @TableField(value = "job_description")
    private String jobDescription;

    @Schema(description = "记录创建时间（执行时间）")
    @TableField(value = "create_time")
    private LocalDateTime createTime;
}
