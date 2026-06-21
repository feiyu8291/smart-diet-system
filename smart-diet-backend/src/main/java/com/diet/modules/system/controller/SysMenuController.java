package com.diet.modules.system.controller;

import com.diet.modules.auth.security.RequiresPermission;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import com.diet.modules.system.model.dto.SysMenuDTO;
import com.diet.modules.system.model.vo.SysMenuVO;
import com.diet.modules.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "系统管理-菜单管理")
@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @RequiresPermission
    @Operation(summary = "获取全部菜单树")
    @GetMapping("/tree")
    public Result<List<SysMenuVO>> tree() {
        return Result.success(sysMenuService.getAllTree());
    }

    @RequiresPermission
    @Operation(summary = "新增菜单")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody @Valid SysMenuDTO dto) {
        sysMenuService.saveOrUpdateMenu(dto);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "修改菜单")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody @Valid SysMenuDTO dto) {
        sysMenuService.saveOrUpdateMenu(dto);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "批量删除菜单")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody @Valid BaseDeleteDTO dto) {
        sysMenuService.deleteByIds(new java.util.ArrayList<>(dto.allIds()));
        return Result.success();
    }
}
