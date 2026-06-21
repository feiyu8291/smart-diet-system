package com.diet.modules.quartz.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diet.modules.auth.security.RequiresPermission;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import com.diet.modules.quartz.model.entity.SysQuartzJob;
import com.diet.modules.quartz.model.entity.SysQuartzJobLog;
import com.diet.modules.quartz.model.po.SysQuartzJobQueryPO;
import com.diet.modules.quartz.model.po.SysQuartzJobSavePO;
import com.diet.modules.quartz.model.po.SysQuartzJobUpdatePO;
import com.diet.modules.quartz.service.SysQuartzJobLogService;
import com.diet.modules.quartz.service.SysQuartzJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务管理 Controller
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "定时任务管理")
@RestController
@RequestMapping("/sys/quartzJob")
@RequiredArgsConstructor
public class SysQuartzJobController {

    private final SysQuartzJobService sysQuartzJobService;
    private final SysQuartzJobLogService sysQuartzJobLogService;

    @RequiresPermission
    @Operation(summary = "定时任务日志分页查询")
    @GetMapping("/log/page")
    public Result<Page<SysQuartzJobLog>> logPage(@Valid com.diet.modules.common.entity.BasePageQuery queryPO) {
        Page<SysQuartzJobLog> logPage = sysQuartzJobLogService.lambdaQuery()
                .orderByDesc(SysQuartzJobLog::getCreateTime)
                .page(new Page<>(queryPO.getPageNo(), queryPO.getPageSize()));
        return Result.success(logPage);
    }

    @RequiresPermission
    @Operation(summary = "定时任务分页查询")
    @GetMapping("/page")
    public Result<Page<SysQuartzJob>> page(@Valid SysQuartzJobQueryPO queryPO) {
        return Result.success(sysQuartzJobService.page(queryPO));
    }

    @RequiresPermission
    @Operation(summary = "新增定时任务")
    @PostMapping("/save")
    public Result<Void> saveJob(@Valid @RequestBody SysQuartzJobSavePO req) {
        sysQuartzJobService.saveJob(req);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "修改定时任务")
    @PostMapping("/update")
    public Result<Void> updateJob(@Valid @RequestBody SysQuartzJobUpdatePO req) {
        sysQuartzJobService.updateJob(req);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "删除定时任务")
    @PostMapping("/delete")
    public Result<Void> deleteJob(@RequestBody @Valid BaseDeleteDTO dto) {
        sysQuartzJobService.deleteJob(new java.util.ArrayList<>(dto.allIds()));
        return Result.success();
    }

    @Operation(summary = "手动触发一次")
    @PostMapping("/trigger/{id}")
    public Result<Void> triggerJob(@Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        sysQuartzJobService.triggerJob(id);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "暂停任务")
    @PostMapping("/pause/{id}")
    public Result<Void> pauseJob(@Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        sysQuartzJobService.pauseJob(id);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "恢复任务")
    @PostMapping("/resume/{id}")
    public Result<Void> resumeJob(@Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        sysQuartzJobService.resumeJob(id);
        return Result.success();
    }
}
