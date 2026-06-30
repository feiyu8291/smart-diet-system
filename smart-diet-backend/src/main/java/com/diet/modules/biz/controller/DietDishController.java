package com.diet.modules.biz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diet.modules.biz.model.dto.DietDishSaveDTO;
import com.diet.modules.biz.model.dto.DietDislikeDTO;
import com.diet.modules.biz.model.dto.DietSkilledDTO;
import com.diet.modules.biz.model.dto.DietWishDTO;
import com.diet.modules.biz.model.po.DietDishQueryPO;
import com.diet.modules.biz.model.vo.DietDishDetailVO;
import com.diet.modules.biz.model.vo.DietDishVO;
import com.diet.modules.biz.service.DietDishService;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜谱管理 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "业务管理-菜谱管理")
@RestController
@RequestMapping("/api/dish")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietDishController {

    private final DietDishService dishService;

    @Operation(summary = "菜谱分页查询")
    @PostMapping("/page")
    public Result<List<DietDishVO>> pageDishes(@RequestBody DietDishQueryPO po) {
        IPage<DietDishVO> page = dishService.pageDishes(po);
        return Result.successPage(page);
    }

    @Operation(summary = "查询菜谱列表")
    @GetMapping("/list")
    public Result<List<DietDishVO>> listDishes(DietDishQueryPO po) {
        List<DietDishVO> list = dishService.listDishes(po);
        return Result.success(list);
    }

    @Operation(summary = "查询菜谱详情")
    @GetMapping("/detail")
    public Result<DietDishDetailVO> getDishDetail(@RequestParam Long dishId) {
        DietDishDetailVO detail = dishService.getDishDetail(dishId);
        return Result.success(detail);
    }

    @Operation(summary = "忌口反馈")
    @PostMapping("/dislike")
    public Result<Boolean> markDislike(@RequestBody DietDislikeDTO dto) {
        Boolean success = dishService.markDislike(dto);
        return Result.success(success);
    }

    @Operation(summary = "添加意向菜品")
    @PostMapping("/wish")
    public Result<Boolean> addWish(@RequestBody DietWishDTO dto) {
        Boolean success = dishService.addWish(dto);
        return Result.success(success);
    }

    @Operation(summary = "拿手菜标记管理")
    @PostMapping("/skilled")
    public Result<Boolean> manageSkilled(@RequestBody DietSkilledDTO dto) {
        Boolean success = dishService.manageSkilled(dto);
        return Result.success(success);
    }

    @Operation(summary = "保存/修改菜谱")
    @PostMapping("/save")
    public Result<Boolean> saveDish(@RequestBody DietDishSaveDTO dto) {
        Boolean success = dishService.saveDish(dto);
        return Result.success(success);
    }

    @Operation(summary = "删除菜谱")
    @PostMapping("/delete")
    public Result<Boolean> deleteDish(@RequestBody BaseDeleteDTO dto) {
        Boolean success = dishService.deleteDishes(dto);
        return Result.success(success);
    }

    @Operation(summary = "删除做法分支")
    @PostMapping("/delete/branch")
    public Result<Boolean> deleteBranch(@RequestBody BaseDeleteDTO dto) {
        Boolean success = dishService.deleteBranches(dto);
        return Result.success(success);
    }
}

