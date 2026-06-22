package com.diet.modules.biz.controller;

import com.diet.modules.biz.model.dto.DietMealCompleteDTO;
import com.diet.modules.biz.model.dto.DietMealPlanSaveDTO;
import com.diet.modules.biz.model.entity.DietDish;
import com.diet.modules.biz.model.entity.DietFamilyMealPlan;
import com.diet.modules.biz.model.po.DietMealDetailQueryPO;
import com.diet.modules.biz.model.po.DietMealRecommendQueryPO;
import com.diet.modules.biz.model.vo.DietDayMealDetailVO;
import com.diet.modules.biz.model.vo.DietMealDetailVO;
import com.diet.modules.biz.service.DietFamilyMealPlanService;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    public Result<List<DietDish>> getRecommendations(DietMealRecommendQueryPO po) {
        LocalDate date = LocalDate.parse(po.getTargetDate());
        List<DietDish> dishes = familyMealPlanService.generateRecommendedMeal(
                po.getGroupId(), date, po.getMealPeriod(), po.getDietMode(), po.getLimit());
        return Result.success(dishes);
    }

    @Operation(summary = "保存联合配餐计划")
    @PostMapping("/save")
    public Result<DietFamilyMealPlan> saveMealPlan(@RequestBody DietMealPlanSaveDTO dto) {
        if (dto == null || dto.getGroupId() == null || dto.getTargetDate() == null
                || dto.getMealPeriod() == null || dto.getDietMode() == null || dto.getDishIds() == null) {
            throw new RuntimeException("参数不完整，保存失败");
        }
        LocalDate date = LocalDate.parse(dto.getTargetDate());
        DietFamilyMealPlan mealPlan = familyMealPlanService.saveMealPlan(
                dto.getGroupId(), date, dto.getMealPeriod(), dto.getDietMode(), dto.getDishIds());
        return Result.success(mealPlan);
    }

    @Operation(summary = "获取联合配餐详情")
    @GetMapping("/detail")
    public Result<DietMealDetailVO> getMealDetail(DietMealDetailQueryPO po) {
        LocalDate date = LocalDate.parse(po.getTargetDate());
        DietMealDetailVO detail = familyMealPlanService.getMealDetail(po.getGroupId(), date, po.getMealPeriod());
        return Result.success(detail);
    }

    @Operation(summary = "获取联合配餐全天详情")
    @GetMapping("/day-detail")
    public Result<DietDayMealDetailVO> getDayMealDetail(DietMealDetailQueryPO po) {
        LocalDate date = LocalDate.parse(po.getTargetDate());
        DietDayMealDetailVO detail = familyMealPlanService.getDayMealDetail(po.getGroupId(), date);
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
