package com.diet.modules.system.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diet.modules.auth.security.RequiresPermission;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import com.diet.modules.system.model.dto.SysDataDictionaryDTO;
import com.diet.modules.system.model.entity.SysDataDictionary;
import com.diet.modules.system.model.po.SysDataDictionaryQueryPO;
import com.diet.modules.system.model.vo.SysDataDictPageVO;
import com.diet.modules.system.model.vo.SysDataDictTypeVO;
import com.diet.modules.system.model.vo.SysDataDictVO;
import com.diet.modules.system.service.SysDataDictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典 Controller
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "系统管理-数据字典")
@RestController
@RequestMapping("/sys/dict")
@RequiredArgsConstructor
public class SysDataDictionaryController {

    private final SysDataDictionaryService sysDataDictionaryService;

    @Operation(summary = "根据类型获取多个数据字典")
    @GetMapping("more")
    public Result<Object> getMore(@Parameter(description = "数据类型", required = true) @NotBlank(message = "数据类型不能为空，请传入有效的类型值") @RequestParam String dataTypes) {
        List<String> dataTypeList = CharSequenceUtil.split(dataTypes, ",");
        Map<String, List<SysDataDictVO>> listMap = sysDataDictionaryService.getDictMapByDataTypes(dataTypeList);
        return Result.success(listMap);
    }

    @Operation(summary = "根据类型获取树形数据字典")
    @GetMapping("tree")
    public Result<Object> getTreeByType(@Parameter(description = "数据类型", required = true) @NotBlank(message = "数据类型不能为空，请传入有效的类型值") @RequestParam String dataType) {
        List<SysDataDictVO> treeList = sysDataDictionaryService.getTreeByType(dataType);
        return Result.success(treeList);
    }

    @RequiresPermission
    @Operation(summary = "数据字典分页查询")
    @GetMapping("/page")
    public Result<Page<SysDataDictPageVO>> page(@Valid SysDataDictionaryQueryPO queryPO) {
        return Result.success(sysDataDictionaryService.page(queryPO));
    }

    @Operation(summary = "数据字典分页查询")
    @GetMapping("/dataTypeAll")
    public Result<List<SysDataDictTypeVO>> dataTypeAll() {
        return Result.success(sysDataDictionaryService.getDataTypeAll());
    }

    @Operation(summary = "获取一级字典标签列表")
    @GetMapping("/firstLevel")
    public Result<List<SysDataDictVO>> getFirstLevel() {
        return Result.success(sysDataDictionaryService.getFirstLevelDicts());
    }

    @Operation(summary = "根据父级类型和父级编码查询子级列表")
    @GetMapping("/children")
    public Result<List<SysDataDictVO>> getChildren(@RequestParam String parentType, @RequestParam String parentCode) {
        return Result.success(sysDataDictionaryService.getChildListByParentTypeAndCode(parentType, parentCode));
    }

    @RequiresPermission
    @Operation(summary = "刷新数据字典缓存")
    @PostMapping("/refreshCache")
    public Result<Void> refreshCache() {
        sysDataDictionaryService.refreshCache();
        return Result.success();
    }

    @Operation(summary = "根据类型查询字典列表")
    @GetMapping("/list")
    public Result<List<SysDataDictionary>> listByType(@Parameter(description = "数据类型", required = true) @RequestParam String dataType) {
        return Result.success(sysDataDictionaryService.listByType(dataType));
    }

    @RequiresPermission
    @Operation(summary = "新增数据字典")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody @Valid SysDataDictionaryDTO dto) {
        sysDataDictionaryService.saveOrUpdateDict(dto);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "修改数据字典")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody @Valid SysDataDictionaryDTO dto) {
        sysDataDictionaryService.saveOrUpdateDict(dto);
        return Result.success();
    }

    @RequiresPermission
    @Operation(summary = "批量删除数据字典")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody @Valid BaseDeleteDTO dto) {
        sysDataDictionaryService.deleteByIds(new java.util.ArrayList<>(dto.allIds()));
        return Result.success();
    }
}
