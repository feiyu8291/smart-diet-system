package com.diet.modules.system.service;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.common.entity.BasePageQuery;
import com.diet.modules.common.util.SecurityUtils;
import com.diet.modules.system.mapper.SysOperationLogMapper;
import com.diet.modules.system.model.entity.SysOperationLog;
import com.diet.modules.system.model.entity.SysUser;
import com.diet.modules.system.model.vo.SysOperationLogVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统操作日志服务业务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Service
public class SysOperationLogService extends ServiceImpl<SysOperationLogMapper, SysOperationLog> {

    /**
     * 分页查询操作日志列表
     */
    public Page<SysOperationLogVO> pageOperationLogs(BasePageQuery query, String realName, String opType, String opModule) {
        Page<SysOperationLog> page = new Page<>(query.getPageNo(), query.getPageSize());
        this.lambdaQuery()
                .like(CharSequenceUtil.isNotBlank(realName), SysOperationLog::getRealName, realName)
                .eq(CharSequenceUtil.isNotBlank(opType), SysOperationLog::getOpType, opType)
                .like(CharSequenceUtil.isNotBlank(opModule), SysOperationLog::getOpModule, opModule)
                .orderByDesc(SysOperationLog::getCreateTime)
                .page(page);

        Page<SysOperationLogVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<SysOperationLogVO> voList = new ArrayList<>();
        for (SysOperationLog entity : page.getRecords()) {
            SysOperationLogVO vo = new SysOperationLogVO();
            vo.copy(entity);
            vo.setId(entity.getId().toString());
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 手动记录操作日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveLog(String opType, String opModule, String content) {
        SysUser user = null;
        try {
            user = SecurityUtils.getCurrentUser();
        } catch (Exception ignored) {
        }
        String username = user != null ? user.getUsername() : "system";
        String realName = user != null ? user.getRealName() : "系统定时任务";
        String ip = getClientIp();

        SysOperationLog operationLog = new SysOperationLog();
        operationLog.setUsername(username);
        operationLog.setRealName(realName);
        operationLog.setIpAddress(ip);
        operationLog.setOpType(opType);
        operationLog.setOpModule(opModule);
        operationLog.setContent(content);
        operationLog.setCreateTime(LocalDateTime.now());
        this.save(operationLog);
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "127.0.0.1";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
