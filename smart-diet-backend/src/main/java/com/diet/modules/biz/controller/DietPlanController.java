package com.diet.modules.biz.controller;

import com.diet.modules.biz.model.dto.DietStartPlanDTO;
import com.diet.modules.biz.model.dto.DietWeightRecordDTO;
import com.diet.modules.biz.model.vo.DietPlanProgressVO;
import com.diet.modules.biz.model.vo.DietPlanVO;
import com.diet.modules.biz.model.vo.DietWeightRecordVO;
import com.diet.modules.biz.service.DietPlanService;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计划进度与体重 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "业务管理-计划进度管理")
@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietPlanController {

    private final DietPlanService planService;

    @Operation(summary = "获取全量配餐计划模板")
    @GetMapping("/templates")
    public Result<List<DietPlanVO>> getTemplates() {
        List<DietPlanVO> templates = planService.getTemplates();
        return Result.success(templates);
    }

    @Operation(summary = "获取当前进行中的计划进度")
    @GetMapping("/current")
    public Result<DietPlanProgressVO> getCurrentProgress(@RequestParam Long groupId) {
        DietPlanProgressVO progress = planService.getCurrentProgress(groupId);
        return Result.success(progress);
    }

    @Operation(summary = "启动新配餐计划模板")
    @PostMapping("/start")
    public Result<Boolean> startPlan(@RequestBody DietStartPlanDTO dto) {
        Boolean success = planService.startPlan(dto);
        return Result.success(success);
    }

    @Operation(summary = "登记成员体重")
    @PostMapping("/weight/record")
    public Result<Boolean> recordWeight(@RequestBody DietWeightRecordDTO dto) {
        Boolean success = planService.recordWeight(dto);
        return Result.success(success);
    }

    @Operation(summary = "获取成员体重变动历史")
    @GetMapping("/weight/history")
    public Result<List<DietWeightRecordVO>> getWeightHistory(@RequestParam Long profileId) {
        List<DietWeightRecordVO> history = planService.getWeightHistory(profileId);
        return Result.success(history);
    }
}
