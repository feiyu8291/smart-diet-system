package com.diet.modules.quartz.config;

import com.diet.modules.quartz.model.entity.SysQuartzJobLog;
import com.diet.modules.quartz.service.DynamicJobService;
import com.diet.modules.quartz.service.SysQuartzJobLogService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

import java.time.LocalDateTime;

/**
 * Quartz 触发器运行监听器
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
public class TriggerRunListener implements TriggerListener {

    private final SysQuartzJobLogService quartzJobEntityLogService;
    private final DynamicJobService dynamicJobService;

    public TriggerRunListener(SysQuartzJobLogService quartzJobEntityLogService,
                              DynamicJobService dynamicJobService) {
        this.quartzJobEntityLogService = quartzJobEntityLogService;
        this.dynamicJobService = dynamicJobService;
    }

    @Override
    public String getName() {
        return "SystemTriggerRunListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        log.warn("=== 监听器捕获：触发器 Misfire ===");
        try {
            JobDataMap jobDataMap = trigger.getJobDataMap();
            if (jobDataMap == null || jobDataMap.isEmpty()) {
                return;
            }
            Long jobId = jobDataMap.getLong("jobId");
            String cronExpression = jobDataMap.getString("cronExpression");

            SysQuartzJobLog jobLog = new SysQuartzJobLog();
            jobLog.setJobId(jobId);
            jobLog.setCronExpression(cronExpression);
            jobLog.setCreateTime(LocalDateTime.now());
            jobLog.setJobDescription("触发器 Misfire (错过触发)");

            quartzJobEntityLogService.save(jobLog);
        } catch (Exception e) {
            log.error("保存Misfire日志异常", e);
        }
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
                                Trigger.CompletedExecutionInstruction triggerInstructionCode) {
    }
}
