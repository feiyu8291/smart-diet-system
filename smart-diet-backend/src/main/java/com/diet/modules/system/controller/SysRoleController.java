package com.diet.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diet.modules.auth.security.RequiresPermission;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import com.diet.modules.system.model.dto.SysRoleDTO;
import com.diet.modules.system.model.dto.SysRoleMenuDTO;
import com.diet.modules.system.model.entity.SysRole;
import com.diet.modules.system.model.po.SysRoleQueryPO;
import com.diet.modules.system.service.SysRoleMenuService;
import com.diet.modules.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "系统管理-角色管理")
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysRoleMenuService sysRoleMenuService;

    @RequiresPermission
    @Operation(summary = "角色分页查询")
    @GetMapping("/page")
    public Result<Page<SysRole>> page(@Valid SysRoleQueryPO queryPO) {
        return Result.success(sysRoleService.page(queryPO));
    }

    @Operation(summary = "查询所有角色")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(sysRoleService.listAll());
    }

    @RequiresPermission
    @Operation(summary = "新增角色（含绑定菜单）")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody @Valid SysRoleDTO dto) {
        sysRoleService.saveOrUpdateRole(dto);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "修改角色（含重新绑定菜单）")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody @Valid SysRoleDTO dto) {
        sysRoleService.saveOrUpdateRole(dto);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "批量删除角色")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody @Valid BaseDeleteDTO dto) {
        sysRoleService.deleteByIds(new java.util.ArrayList<>(dto.allIds()));
        return Result.success();
    }

    @Operation(summary = "查询角色已关联的菜单ID列表")
    @GetMapping("/menuIds")
    public Result<List<Long>> getMenuIds(
            @Parameter(description = "角色ID", required = true) @RequestParam Long roleId) {
        return Result.success(sysRoleMenuService.listMenuIdsByRoleId(roleId));
    }

    @RequiresPermission
    @Operation(summary = "配置角色菜单", description = "为指定角色绑定菜单列表，会先清除原有绑定再重新关联")
    @PostMapping("/configMenus")
    public Result<Void> configMenus(@RequestBody @Valid SysRoleMenuDTO dto) {
        sysRoleService.configMenus(dto);
        return Result.success();
    }
}
