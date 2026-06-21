package com.diet.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.system.model.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper 接口
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {
}
