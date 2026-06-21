package com.diet.modules.quartz.config;

import com.diet.modules.quartz.service.DynamicJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动时初始化Quartz任务
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Component
@Order(10)
public class QuartzInitRunner implements ApplicationRunner {

    private final DynamicJobService dynamicJobService;

    public QuartzInitRunner(DynamicJobService dynamicJobService) {
        this.dynamicJobService = dynamicJobService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("--- 开始初始化加载定时任务 ---");
            dynamicJobService.reStartAllJobs();
            log.info("--- 定时任务加载完毕 ---");
        } catch (Exception e) {
            log.error("初始化加载定时任务异常", e);
        }
    }
}
