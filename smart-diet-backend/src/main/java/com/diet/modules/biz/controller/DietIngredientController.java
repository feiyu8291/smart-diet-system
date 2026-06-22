package com.diet.modules.biz.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.diet.modules.biz.model.entity.DietIngredient;
import com.diet.modules.biz.service.DietIngredientService;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 原材料管理 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Tag(name = "业务管理-原材料管理")
@RestController
@RequestMapping("/api/ingredient")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietIngredientController {

    private final DietIngredientService ingredientService;

    @Operation(summary = "查询原材料列表")
    @GetMapping("/list")
    public Result<List<DietIngredient>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer condimentFlag) {

        LambdaQueryWrapper<DietIngredient> query = new LambdaQueryWrapper<>();
        query.eq(DietIngredient::getDelFlag, 0);
        if (name != null && !name.trim().isEmpty()) {
            query.like(DietIngredient::getIngredientName, name.trim());
        }
        if (condimentFlag != null) {
            query.eq(DietIngredient::getCondimentFlag, condimentFlag);
        }
        query.orderByAsc(DietIngredient::getIngredientId);

        List<DietIngredient> list = ingredientService.list(query);
        return Result.success(list);
    }

    @Operation(summary = "保存/修改原材料")
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody DietIngredient entity) {
        if (entity == null) {
            return Result.failed("参数不能为空");
        }
        if (entity.getIngredientId() != null) {
            entity.setUpdateTime(LocalDateTime.now());
        } else {
            entity.setDelFlag(0);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
        }
        boolean success = ingredientService.saveOrUpdate(entity);
        return Result.success(success);
    }

    @Operation(summary = "删除原材料")
    @DeleteMapping("/delete/{ingredientId}")
    public Result<Boolean> delete(@PathVariable Long ingredientId) {
        DietIngredient entity = ingredientService.getById(ingredientId);
        if (entity != null) {
            entity.setDelFlag(1); // 软删除
            entity.setUpdateTime(LocalDateTime.now());
            boolean success = ingredientService.updateById(entity);
            return Result.success(success);
        }
        return Result.success(false);
    }
}
