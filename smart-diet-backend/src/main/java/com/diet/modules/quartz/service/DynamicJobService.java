package com.diet.modules.quartz.service;

import cn.hutool.core.bean.BeanUtil;
import com.diet.modules.quartz.config.TriggerRunListener;
import com.diet.modules.quartz.model.entity.SysQuartzJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Quartz调度器核心操作服务
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
@Slf4j
public class DynamicJobService {

    private final SysQuartzJobService sysQuartzJobService;
    private final SysQuartzJobLogService sysQuartzJobLogService;
    private final SchedulerFactoryBean schedulerFactoryBean;

    // 使用Lazy避免循环依赖
    public DynamicJobService(@Lazy SysQuartzJobService sysQuartzJobService,
                             @Lazy SysQuartzJobLogService sysQuartzJobLogService,
                             SchedulerFactoryBean schedulerFactoryBean) {
        this.sysQuartzJobService = sysQuartzJobService;
        this.sysQuartzJobLogService = sysQuartzJobLogService;
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    private JobDataMap getJobDataMap(SysQuartzJob job) {
        return new JobDataMap(BeanUtil.beanToMap(job));
    }

    private JobDetail getJobDetail(JobKey jobKey, String description, JobDataMap map) {
        return JobBuilder.newJob(DynamicJob.class)
                .withIdentity(jobKey)
                .withDescription(description)
                .setJobData(map)
                .storeDurably()
                .build();
    }

    private Trigger getTrigger(SysQuartzJob job) {
        return TriggerBuilder.newTrigger()
                .withIdentity(job.getJobName(), job.getJobGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                .build();
    }

    private JobKey getJobKey(SysQuartzJob job) {
        return JobKey.jobKey(job.getJobName(), job.getJobGroup());
    }

    public void addJobToScheduler(SysQuartzJob job) throws SchedulerException {
        JobDataMap map = this.getJobDataMap(job);
        JobKey jobKey = this.getJobKey(job);
        JobDetail jobDetail = this.getJobDetail(jobKey, job.getJobDescription(), map);
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.scheduleJob(jobDetail, this.getTrigger(job));
    }

    public void removeJobFromScheduler(SysQuartzJob entity) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        Trigger trigger = this.getTrigger(entity);
        scheduler.pauseTrigger(trigger.getKey());
        scheduler.pauseJob(this.getJobKey(entity));
        scheduler.unscheduleJob(trigger.getKey());
        scheduler.deleteJob(this.getJobKey(entity));
    }

    public void modifySchedulerJob(SysQuartzJob job, String newCron) throws SchedulerException {
        JobKey jobKey = this.getJobKey(job);
        TriggerKey triggerKey = new TriggerKey(jobKey.getName(), jobKey.getGroup());
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(newCron);
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobKey.getName(), jobKey.getGroup())
                .withSchedule(cronScheduleBuilder)
                .usingJobData(this.getJobDataMap(job))
                .build();
        scheduler.rescheduleJob(triggerKey, trigger);
    }

    public void pauseJob(SysQuartzJob job) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.pauseJob(this.getJobKey(job));
    }

    public void resumeJob(SysQuartzJob job) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.resumeJob(this.getJobKey(job));
    }

    public void triggerJob(SysQuartzJob job) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = this.getJobKey(job);

        if (!scheduler.checkExists(jobKey)) {
            JobDetail jobDetail = this.getJobDetail(jobKey, job.getJobDescription(), this.getJobDataMap(job));
            scheduler.addJob(jobDetail, true);
        }

        scheduler.triggerJob(jobKey, this.getJobDataMap(job));
    }

    public void reStartAllJobs() throws SchedulerException {
        synchronized (log) {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            Set<JobKey> set = scheduler.getJobKeys(GroupMatcher.anyGroup());
            scheduler.pauseJobs(GroupMatcher.anyGroup());

            for (JobKey jobKey : set) {
                scheduler.unscheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
                scheduler.deleteJob(jobKey);
            }

            // 获取未停用的有效任务
            List<SysQuartzJob> jobEntityList = sysQuartzJobService.lambdaQuery()
                    .and(wq -> wq.eq(SysQuartzJob::getPermanentState, 1)
                            .or().ge(SysQuartzJob::getEndTime, LocalDateTime.now()))
                    .list();

            // 添加全局监听器记录Misfire等
            scheduler.getListenerManager().addTriggerListener(new TriggerRunListener(sysQuartzJobLogService, this));

            for (SysQuartzJob job : jobEntityList) {
                log.info("注册定时任务:  名称={}, 表达式={}", job.getJobName(), job.getCronExpression());
                JobDataMap jobDataMap = this.getJobDataMap(job);
                JobKey jobKey = this.getJobKey(job);
                JobDetail jobDetail = this.getJobDetail(jobKey, job.getJobDescription(), jobDataMap);
                scheduler.scheduleJob(jobDetail, this.getTrigger(job));
            }
        }
    }
}
