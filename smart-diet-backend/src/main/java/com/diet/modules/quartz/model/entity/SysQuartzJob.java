package com.diet.modules.quartz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 定时任务实体类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_quartz_job")
@Schema(description = "定时任务实体")
public class SysQuartzJob extends BaseEntity {

    @Schema(description = "主键ID")
    @TableId(value = "job_id", type = IdType.AUTO)
    private Long jobId;

    @Schema(description = "计划名字")
    @TableField(value = "job_name")
    private String jobName;

    @Schema(description = "分组")
    @TableField(value = "job_group")
    private String jobGroup;

    @Schema(description = "cron表达式")
    @TableField(value = "cron_expression")
    private String cronExpression;

    @Schema(description = "描述")
    @TableField(value = "job_description")
    private String jobDescription;

    @Schema(description = "Spring Bean名称(如: demoQuartzJobHandler)")
    @TableField(value = "handle_class")
    private String handleClass;

    @Schema(description = "方法参数")
    @TableField(value = "method_param")
    private String methodParam;

    @Schema(description = "任务结束时间")
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    @Schema(description = "是否永久有效 0否1是")
    @TableField(value = "permanent_state")
    private Integer permanentState;

    @Schema(description = "任务类型 1：系统任务，2：业务任务")
    @TableField(value = "job_type")
    private Integer jobType;
}
