package com.diet.modules.biz.controller;

import cn.hutool.core.collection.CollUtil;
import com.diet.modules.biz.model.dto.DietMealCompleteDTO;
import com.diet.modules.biz.model.dto.DietMealPlanSaveDTO;
import com.diet.modules.biz.model.dto.DietMealPublishDTO;
import com.diet.modules.biz.model.dto.DietMealRecommendQueryDTO;
import com.diet.modules.biz.model.entity.DietMealFeedback;
import com.diet.modules.biz.model.po.DietMealDetailQueryPO;
import com.diet.modules.biz.model.po.DietMealRecommendQueryPO;
import com.diet.modules.biz.model.po.DietMemberMealPortionQueryPO;
import com.diet.modules.biz.model.vo.*;
import com.diet.modules.biz.service.DietFamilyMealPlanService;
import com.diet.modules.biz.service.DietMealFeedbackService;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final DietMealFeedbackService mealFeedbackService;

    @Operation(summary = "查询今日联合配餐计划详情")
    @GetMapping("/current-day")
    public Result<DietDayMealDetailVO> getCurrentDayMeal(@RequestParam Long groupId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        DietDayMealDetailVO detail = familyMealPlanService.getDayMealDetail(groupId, localDate);
        return Result.success(detail);
    }

    @Operation(summary = "查询成员当天的就餐分量列表")
    @PostMapping("/portions")
    public Result<List<DietMemberMealPortionVO>> getPortionsByMember(@RequestBody DietMemberMealPortionQueryPO po) {
        if (po == null || po.getGroupId() == null || po.getProfileId() == null || po.getDate() == null) {
            return Result.success(Collections.emptyList());
        }
        List<DietMemberMealPortionVO> list = familyMealPlanService.getPortionsByMember(po);
        return Result.success(list);
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
    @GetMapping("/list-today")
    public Result<List<DietMealFeedback>> listTodayFeedback(@RequestParam Long profileId) {
        List<DietMealFeedback> list = mealFeedbackService.listTodayFeedback(profileId);
        return Result.success(list);
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

    @Operation(summary = "更新配餐菜品烹饪完成状态")
    @PostMapping("/cook-dish")
    public Result<Boolean> updateDishCookFlag(@RequestParam Long mealPlanId, @RequestParam Long branchId, @RequestParam Integer cookFlag) {
        Boolean success = familyMealPlanService.updateDishCookFlag(mealPlanId, branchId, cookFlag);
        return Result.success(success);
    }

    @Operation(summary = "智能推荐生成全天三餐膳食计划")
    @PostMapping("/generate-recommend")
    public Result<Map<String, List<Map<String, Object>>>> generateRecommend(
            @RequestBody DietMealRecommendQueryDTO dto) {
        if (dto == null || dto.getGroupId() == null || dto.getMealDate() == null) {
            return Result.success(Collections.emptyMap());
        }
        LocalDate date = LocalDate.parse(dto.getMealDate());
        Integer mode = dto.getDietMode() != null ? dto.getDietMode() : 0;
        Map<String, List<Map<String, Object>>> res = familyMealPlanService.generateRecommend(dto.getGroupId(), date,
                mode);
        return Result.success(res);
    }

    @Operation(summary = "发布确认全天排餐食谱")
    @PostMapping("/publish")
    public Result<Boolean> publishMealPlan(@RequestBody DietMealPublishDTO dto) {
        if (dto == null || dto.getGroupId() == null || dto.getMealDate() == null) {
            return Result.success(false);
        }
        LocalDate date = LocalDate.parse(dto.getMealDate());
        Boolean success = familyMealPlanService.publishMealPlan(dto.getGroupId(), date, dto.getMenuData());
        return Result.success(success);
    }
}
