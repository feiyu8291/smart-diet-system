package com.diet.modules.quartz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.quartz.model.entity.SysQuartzJobLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务日志 Mapper
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Mapper
public interface SysQuartzJobLogMapper extends BaseMapper<SysQuartzJobLog> {
}
