package com.diet.modules.biz.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.diet.modules.biz.model.entity.DietCookingStepPool;
import com.diet.modules.biz.service.DietCookingStepPoolService;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 烹饪标准步骤模板池 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Tag(name = "业务管理-操作步骤模板")
@RestController
@RequestMapping("/api/step")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietCookingStepPoolController {

    private final DietCookingStepPoolService cookingStepPoolService;

    @Operation(summary = "查询标准步骤模板列表")
    @GetMapping("/list")
    public Result<List<DietCookingStepPool>> list(@RequestParam(required = false) String name) {
        LambdaQueryWrapper<DietCookingStepPool> query = new LambdaQueryWrapper<>();
        query.eq(DietCookingStepPool::getDelFlag, 0);
        if (name != null && !name.trim().isEmpty()) {
            query.like(DietCookingStepPool::getStepName, name.trim());
        }
        query.orderByAsc(DietCookingStepPool::getStepPoolId);

        List<DietCookingStepPool> list = cookingStepPoolService.list(query);
        return Result.success(list);
    }

    @Operation(summary = "保存/修改步骤模板")
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody DietCookingStepPool entity) {
        if (entity == null) {
            return Result.failed("参数不能为空");
        }
        if (entity.getStepPoolId() != null) {
            entity.setUpdateTime(LocalDateTime.now());
        } else {
            entity.setDelFlag(0);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
        }
        boolean success = cookingStepPoolService.saveOrUpdate(entity);
        return Result.success(success);
    }

    @Operation(summary = "删除步骤模板")
    @DeleteMapping("/delete/{stepPoolId}")
    public Result<Boolean> delete(@PathVariable Long stepPoolId) {
        DietCookingStepPool entity = cookingStepPoolService.getById(stepPoolId);
        if (entity != null) {
            entity.setDelFlag(1); // 软删除
            entity.setUpdateTime(LocalDateTime.now());
            boolean success = cookingStepPoolService.updateById(entity);
            return Result.success(success);
        }
        return Result.success(false);
    }
}
