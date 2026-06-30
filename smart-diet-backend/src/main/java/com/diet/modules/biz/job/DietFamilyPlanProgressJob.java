package com.diet.modules.biz.job;

import com.diet.modules.biz.mapper.DietPlanMapper;
import com.diet.modules.biz.model.entity.DietFamilyPlanProgress;
import com.diet.modules.biz.model.entity.DietPlan;
import com.diet.modules.biz.service.DietFamilyPlanProgressService;
import com.diet.modules.quartz.model.dto.QuartzJobEntityDTO;
import com.diet.modules.quartz.service.IQuartzJobHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component("dietFamilyPlanProgressJob")
@RequiredArgsConstructor
public class DietFamilyPlanProgressJob implements IQuartzJobHandler {

    private final DietFamilyPlanProgressService familyPlanProgressService;
    private final DietPlanMapper dietPlanMapper;

    @Override
    public String execute(QuartzJobEntityDTO jobDTO) {
        log.info("开始执行家庭膳食计划天数自动向前推进任务，时间: {}", LocalDateTime.now());

        List<DietFamilyPlanProgress> activeProgressList = familyPlanProgressService.lambdaQuery()
                .eq(DietFamilyPlanProgress::getProgressStatus, 1)
                .eq(DietFamilyPlanProgress::getDelFlag, 0)
                .list();

        if (activeProgressList.isEmpty()) {
            log.info("未发现进行中的家庭膳食计划，定时任务结束");
            return "未发现进行中的家庭膳食计划，执行结束。";
        }

        LocalDate today = LocalDate.now();
        int successCount = 0;
        int completeCount = 0;

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
                        completeCount++;
                        log.info("家庭组 {} 的计划 {} (第 {} 天) 超过模板总天数 {}，自动标记为已完成",
                                progress.getGroupId(), progress.getPlanId(), currentDay, totalDays);
                    }
                }

                progress.setCurrentDay(currentDay);
                progress.setUpdateTime(LocalDateTime.now());
                familyPlanProgressService.updateById(progress);
                successCount++;

                log.info("成功更新家庭组 {} 的膳食计划进度，当前为第 {} 天", progress.getGroupId(), currentDay);

            } catch (Exception e) {
                log.error("更新计划进度 ID: {} 发生异常: ", progress.getProgressId(), e);
            }
        }
        String resultMsg = String.format("家庭膳食计划天数自动推进完毕。更新成功: %d 个，自动标记完成: %d 个。", successCount, completeCount);
        log.info(resultMsg);
        return resultMsg;
    }
}
