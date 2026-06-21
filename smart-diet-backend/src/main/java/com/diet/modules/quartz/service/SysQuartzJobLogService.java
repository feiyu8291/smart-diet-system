package com.diet.modules.quartz.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.quartz.mapper.SysQuartzJobLogMapper;
import com.diet.modules.quartz.model.entity.SysQuartzJobLog;
import org.springframework.stereotype.Service;

/**
 * 定时任务日志 服务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
public class SysQuartzJobLogService extends ServiceImpl<SysQuartzJobLogMapper, SysQuartzJobLog> {
}
