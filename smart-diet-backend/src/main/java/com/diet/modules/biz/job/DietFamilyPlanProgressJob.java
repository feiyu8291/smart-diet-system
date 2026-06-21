package com.diet.modules.biz.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.diet.modules.biz.mapper.DietPlanMapper;
import com.diet.modules.biz.mapper.DietFamilyPlanProgressMapper;
import com.diet.modules.biz.model.entity.DietPlan;
import com.diet.modules.biz.model.entity.DietFamilyPlanProgress;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 家庭膳食计划进度自动推进定时任务
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Component
public class DietFamilyPlanProgressJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(DietFamilyPlanProgressJob.class);

    @Autowired
    private DietFamilyPlanProgressMapper familyPlanProgressMapper;

    @Autowired
    private DietPlanMapper dietPlanMapper;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行家庭膳食计划天数自动向前推进任务，时间: {}", LocalDateTime.now());

        LambdaQueryWrapper<DietFamilyPlanProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DietFamilyPlanProgress::getProgressStatus, 1)
                .eq(DietFamilyPlanProgress::getDelFlag, 0);
        List<DietFamilyPlanProgress> activeProgressList = familyPlanProgressMapper.selectList(queryWrapper);

        if (activeProgressList.isEmpty()) {
            log.info("未发现进行中的家庭膳食计划，定时任务结束");
            return;
        }

        LocalDate today = LocalDate.now();

        for (DietFamilyPlanProgress progress : activeProgressList) {
            try {
                LocalDate start = progress.getStartDate();
                if (start == null) {
                    continue;
                }

                long daysBetween = ChronoUnit.DAYS.between(start, today);
                int currentDay = (int) daysBetween + 1;

                if (currentDay < 1) {
                    currentDay = 1;
                }

                DietPlan template = dietPlanMapper.selectById(progress.getPlanId());
                if (template != null) {
                    int totalDays = template.getTotalDays();
                    if (currentDay > totalDays) {
                        progress.setProgressStatus(2); // 标记完成
                        log.info("家庭组 {} 的计划 {} (第 {} 天) 超过模板总天数 {}，自动标记为已完成",
                                progress.getGroupId(), progress.getPlanId(), currentDay, totalDays);
                    }
                }

                progress.setCurrentDay(currentDay);
                progress.setUpdateTime(LocalDateTime.now());
                familyPlanProgressMapper.updateById(progress);

                log.info("成功更新家庭组 {} 的膳食计划进度，当前为第 {} 天", progress.getGroupId(), currentDay);

            } catch (Exception e) {
                log.error("更新计划进度 ID: {} 发生异常: ", progress.getProgressId(), e);
            }
        }
        log.info("家庭膳食计划天数自动推进任务执行完毕");
    }
}
