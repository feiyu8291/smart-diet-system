package com.diet.modules.system.service;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.common.entity.BasePageQuery;
import com.diet.modules.system.mapper.SysLoginLogMapper;
import com.diet.modules.system.model.entity.SysLoginLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 系统登录日志服务业务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
public class SysLoginLogService extends ServiceImpl<SysLoginLogMapper, SysLoginLog> {

    /**
     * 分页查询登录日志
     */
    public Page<SysLoginLog> pageLoginLogs(BasePageQuery query, String username, String realName, Integer status) {
        Page<SysLoginLog> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();

        if (CharSequenceUtil.isNotBlank(username)) {
            wrapper.like(SysLoginLog::getUsername, username);
        }
        if (CharSequenceUtil.isNotBlank(realName)) {
            wrapper.like(SysLoginLog::getRealName, realName);
        }
        if (status != null) {
            wrapper.eq(SysLoginLog::getStatus, status);
        }

        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        return this.page(page, wrapper);
    }

    /**
     * 记录登录日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordLoginLog(String username, String realName, String ip, Integer status, String msg) {
        SysLoginLog log = new SysLoginLog();
        log.setUsername(username);
        log.setRealName(realName);
        log.setLoginIp(ip);
        log.setLoginTime(LocalDateTime.now());
        log.setStatus(status);
        log.setMsg(msg);
        this.save(log);
    }
}
