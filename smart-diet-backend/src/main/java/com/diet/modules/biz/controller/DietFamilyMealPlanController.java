package com.diet.modules.biz.controller;

import cn.hutool.core.collection.CollUtil;
import com.diet.modules.biz.model.dto.DietMealCompleteDTO;
import com.diet.modules.biz.model.dto.DietMealPlanSaveDTO;
import com.diet.modules.biz.model.po.DietMealDetailQueryPO;
import com.diet.modules.biz.model.po.DietMealRecommendQueryPO;
import com.diet.modules.biz.model.vo.*;
import com.diet.modules.biz.service.DietFamilyMealPlanService;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 联合配餐 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "业务管理-联合配餐")
@RestController
@RequestMapping("/api/meal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietFamilyMealPlanController {

    private final DietFamilyMealPlanService familyMealPlanService;

    @Operation(summary = "查询今日联合配餐计划详情")
    @GetMapping("/current-day")
    public Result<DietDayMealDetailVO> getCurrentDayMeal(@RequestParam Long groupId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        DietDayMealDetailVO detail = familyMealPlanService.getDayMealDetail(groupId, localDate);
        return Result.success(detail);
    }

    @Operation(summary = "查询今日家庭膳食采购清单列表")
    @GetMapping("/grocery-list")
    public Result<List<DietGroceryVO>> getGroceryList(@RequestParam Long groupId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        DietDayMealDetailVO detail = familyMealPlanService.getDayMealDetail(groupId, localDate);
        if (Objects.nonNull(detail) && CollUtil.isNotEmpty(detail.getDailyGroceries())) {
            return Result.success(detail.getDailyGroceries());
        }
        return Result.success(Collections.emptyList());
    }

    @Operation(summary = "查询今日就餐打卡反馈列表")
    @GetMapping("/feedback/list-today")
    public Result<List<Object>> listTodayFeedback(@RequestParam Long profileId) {
        return Result.success(Collections.emptyList());
    }

    @Operation(summary = "查询避重冷却天数")
    @GetMapping("/cooldown")
    public Result<Integer> getCooldownDays(@RequestParam Long groupId) {
        Integer days = familyMealPlanService.getCooldownDays(groupId);
        return Result.success(days);
    }

    @Operation(summary = "设置避重冷却天数")
    @PostMapping("/cooldown")
    public Result<Boolean> saveCooldownDays(@RequestParam Long groupId, @RequestParam Integer cooldownDays) {
        Boolean success = familyMealPlanService.saveCooldownDays(groupId, cooldownDays);
        return Result.success(success);
    }

    @Operation(summary = "获取联合配餐推荐菜品")
    @GetMapping("/recommend")
    public Result<List<DietDishBranchVO>> getRecommendations(DietMealRecommendQueryPO po) {
        List<DietDishBranchVO> branches = familyMealPlanService.getRecommendations(po);
        return Result.success(branches);
    }

    @Operation(summary = "保存联合配餐计划")
    @PostMapping("/save")
    public Result<DietFamilyMealPlanVO> saveMealPlan(@RequestBody DietMealPlanSaveDTO dto) {
        DietFamilyMealPlanVO mealPlan = familyMealPlanService.saveMealPlan(dto);
        return Result.success(mealPlan);
    }

    @Operation(summary = "获取联合配餐详情")
    @GetMapping("/detail")
    public Result<DietMealDetailVO> getMealDetail(DietMealDetailQueryPO po) {
        DietMealDetailVO detail = familyMealPlanService.getMealDetail(po);
        return Result.success(detail);
    }

    @Operation(summary = "获取联合配餐全天详情")
    @GetMapping("/day-detail")
    public Result<DietDayMealDetailVO> getDayMealDetail(DietMealDetailQueryPO po) {
        DietDayMealDetailVO detail = familyMealPlanService.getDayMealDetail(po);
        return Result.success(detail);
    }

    @Operation(summary = "打卡完成就餐")
    @PostMapping("/complete")
    public Result<Boolean> completeMeal(@RequestBody DietMealCompleteDTO dto) {
        if (dto == null || dto.getMealPlanId() == null) {
            return Result.success(false);
        }
        familyMealPlanService.completeMeal(dto.getMealPlanId(), dto.getDislikes());
        return Result.success(true);
    }
}
