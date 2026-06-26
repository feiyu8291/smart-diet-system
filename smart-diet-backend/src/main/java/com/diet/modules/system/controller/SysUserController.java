package com.diet.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diet.modules.auth.security.RequiresPermission;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import com.diet.modules.system.model.dto.ChangePasswordDTO;
import com.diet.modules.system.model.dto.SysUserDTO;
import com.diet.modules.system.model.entity.SysRole;
import com.diet.modules.system.model.entity.SysUser;
import com.diet.modules.system.model.po.SysUserQueryPO;
import com.diet.modules.system.model.vo.SysUserInfoVO;
import com.diet.modules.system.model.vo.SysUserVO;
import com.diet.modules.system.service.SysRoleService;
import com.diet.modules.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "系统管理-用户管理")
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;

    @RequiresPermission
    @Operation(summary = "用户分页查询")
    @GetMapping("/page")
    public Result<IPage<SysUserVO>> page(@Valid SysUserQueryPO queryPO) {
        return Result.success(sysUserService.page(queryPO));
    }

    @RequiresPermission
    @Operation(summary = "新增用户")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody @Valid SysUserDTO dto) {
        sysUserService.saveOrUpdateUser(dto);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "修改用户")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody @Valid SysUserDTO dto) {
        sysUserService.saveOrUpdateUser(dto);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "启用/禁用用户")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "状态：0启用 1禁用", required = true) @RequestParam Integer useStatus) {
        sysUserService.updateStatus(userId, useStatus);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "批量删除用户")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody @Valid BaseDeleteDTO dto) {
        sysUserService.deleteByIds(new java.util.ArrayList<>(dto.allIds()));
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "重置密码")
    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        sysUserService.resetPassword(userId);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        sysUserService.changePassword(dto);
        return Result.success();
    }

    @Operation(summary = "获取全量角色(无分页)")
    @GetMapping("/getAllRoles")
    public Result<List<SysRole>> getAllRoles() {
        return Result.success(sysRoleService.listAll());
    }

    @Operation(summary = "获取全量用户(无分页)")
    @GetMapping("/listAll")
    public Result<List<SysUser>> listAll() {
        return Result.success(sysUserService.listAll());
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/getCurrentUserInfo")
    public Result<SysUserInfoVO> getCurrentUserInfo() {
        return Result.success(sysUserService.getCurrentUserInfo());
    }
}
