package com.diet.modules.quartz.service;

import com.diet.modules.quartz.model.dto.QuartzJobEntityDTO;

/**
 * Quartz定时任务执行接口
 *
 * @author FeiYu
 * @date 2026-06-20
 */
public interface IQuartzJobHandler {

    /**
     * 执行方法
     *
     * @param jobDTO 执行参数
     * @return 返回执行结果消息
     */
    String execute(QuartzJobEntityDTO jobDTO);
}
