package com.diet.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diet.modules.common.entity.BasePageQuery;
import com.diet.modules.common.entity.Result;
import com.diet.modules.system.model.entity.SysLoginLog;
import com.diet.modules.system.model.vo.SysOperationLogVO;
import com.diet.modules.system.service.SysLoginLogService;
import com.diet.modules.system.service.SysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统-日志管理 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "系统管理-日志模块")
@RestController
@RequestMapping("/sys/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLoginLogService sysLoginLogService;
    private final SysOperationLogService sysOperationLogService;

    @Operation(summary = "登录日志分页列表")
    @GetMapping("/login/page")
    public Result<Page<SysLoginLog>> pageLoginLogs(
            @Valid BasePageQuery query,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status) {
        Page<SysLoginLog> page = sysLoginLogService.pageLoginLogs(query, username, realName, status);
        return Result.success(page);
    }

    @Operation(summary = "操作日志分页列表")
    @GetMapping("/operation/page")
    public Result<Page<SysOperationLogVO>> pageOperationLogs(
            @Valid BasePageQuery query,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String opType,
            @RequestParam(required = false) String opModule) {
        Page<SysOperationLogVO> page = sysOperationLogService.pageOperationLogs(query, realName, opType, opModule);
        return Result.success(page);
    }
}
