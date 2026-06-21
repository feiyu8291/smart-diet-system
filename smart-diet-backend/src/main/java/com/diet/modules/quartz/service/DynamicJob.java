package com.diet.modules.quartz.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.diet.modules.common.config.SpringBeanHolder;
import com.diet.modules.quartz.model.dto.QuartzJobEntityDTO;
import com.diet.modules.quartz.model.entity.SysQuartzJob;
import com.diet.modules.quartz.model.entity.SysQuartzJobLog;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 动态任务类，所有的Quartz定时任务实际上都是这一个类的实例
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Component
@DisallowConcurrentExecution
public class DynamicJob implements Job {

    @Autowired
    private SysQuartzJobLogService jobLogService;
    @Autowired
    private SysQuartzJobService jobService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        Long jobId = jobDataMap.getLong("jobId");
        String cronExpression = jobDataMap.getString("cronExpression");
        String handleClass = jobDataMap.getString("handleClass");
        String methodParam = jobDataMap.getString("methodParam");
        String jobName = jobDataMap.getString("jobName");
        String jobDescription = jobDataMap.getString("jobDescription");

        SysQuartzJobLog jobLog = new SysQuartzJobLog();
        jobLog.setJobId(jobId);
        jobLog.setCronExpression(cronExpression);
        jobLog.setCreateTime(LocalDateTime.now());

        try {
            if (StrUtil.isBlank(handleClass)) {
                throw new IllegalArgumentException("处理Bean名称(handleClass)不能为空");
            }

            // 获取Spring中管理的Bean实例，直接通过 Bean Name 获取 IQuartzJobHandler
            IQuartzJobHandler taskHandler;
            try {
                taskHandler = SpringBeanHolder.getBean(handleClass, IQuartzJobHandler.class);
            } catch (Exception ex) {
                throw new IllegalArgumentException("无法在Spring容器中找到名为 [" + handleClass + "] 且实现 IQuartzJobHandler 接口的 Bean", ex);
            }

            // 构造传递给任务的DTO参数
            QuartzJobEntityDTO jobDTO = new QuartzJobEntityDTO().setJobId(jobId).setJobName(jobName).setCronExpression(cronExpression)
                    .setJobDescription(jobDescription).setHandleClass(handleClass).setMethodParam(methodParam);

            // 执行任务
            String resultMsg = taskHandler.execute(jobDTO);

            jobLog.setJobDescription("任务执行成功：" + resultMsg);
            log.info("执行定时任务成功：[{}], 类: {}, 返回消息: {}", jobName, handleClass, resultMsg);

        } catch (Exception e) {
            String errorMsg = ExceptionUtil.stacktraceToString(e, 500);
            jobLog.setJobDescription("执行异常：" + errorMsg);
            log.error("执行定时任务失败：[{}], 异常：", jobName, e);
        } finally {
            // 记录日志
            try {
                jobLogService.save(jobLog);
            } catch (Exception ex) {
                log.error("保存定时任务日志失败：", ex);
            }

            // 检查如果任务是非永久有效且到达结束时间，则删除它
            checkJobExpiration(context, jobId);
        }
    }

    private void checkJobExpiration(JobExecutionContext context, Long jobId) {
        try {
            SysQuartzJob job = jobService.getById(jobId);
            if (job != null && 0 == job.getPermanentState() && job.getEndTime() != null &&
                    LocalDateTime.now().isAfter(job.getEndTime())) {
                log.info("定时任务 [{}] 已到期，正在从调度器中移除...", job.getJobName());
                context.getScheduler().deleteJob(context.getJobDetail().getKey());
                // 业务表也要逻辑删除或停用
                job.setDelFlag(1);
                jobService.updateById(job);
            }

        } catch (Exception e) {
            log.error("检查定时任务过期状态异常：", e);
        }
    }
}
