package com.diet.modules.biz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diet.modules.biz.model.entity.DietIngredient;
import com.diet.modules.biz.model.po.DietIngredientQueryPO;
import com.diet.modules.biz.model.vo.DietIngredientVO;
import com.diet.modules.biz.service.DietIngredientService;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "原材料分页查询")
    @PostMapping("/page")
    public Result<List<DietIngredientVO>> page(@RequestBody DietIngredientQueryPO po) {
        IPage<DietIngredientVO> pageResult = ingredientService.pageIngredients(po);
        return Result.successPage(pageResult);
    }

    @Operation(summary = "查询原材料列表")
    @GetMapping("/list")
    public Result<List<DietIngredientVO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer ingredientType) {
        List<DietIngredientVO> list = ingredientService.listIngredients(name, ingredientType);
        return Result.success(list);
    }

    @Operation(summary = "保存/修改原材料")
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody DietIngredient entity) {
        if (entity == null) {
            return Result.failed("参数不能为空");
        }
        boolean success = ingredientService.saveIngredient(entity);
        return Result.success(success);
    }

    @Operation(summary = "删除原材料")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody BaseDeleteDTO dto) {
        boolean success = ingredientService.deleteIngredients(dto);
        return Result.success(success);
    }
}

