package com.diet.modules.biz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diet.modules.biz.model.entity.DietCookingStepPool;
import com.diet.modules.biz.model.po.DietCookingStepQueryPO;
import com.diet.modules.biz.model.vo.DietCookingStepPoolVO;
import com.diet.modules.biz.service.DietCookingStepPoolService;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "标准步骤模板分页查询")
    @PostMapping("/page")
    public Result<List<DietCookingStepPoolVO>> page(@RequestBody DietCookingStepQueryPO po) {
        IPage<DietCookingStepPoolVO> pageResult = cookingStepPoolService.pageSteps(po);
        return Result.successPage(pageResult);
    }

    @Operation(summary = "查询标准步骤模板列表")
    @GetMapping("/list")
    public Result<List<DietCookingStepPoolVO>> list(@RequestParam(required = false) String name) {
        List<DietCookingStepPoolVO> list = cookingStepPoolService.listSteps(name);
        return Result.success(list);
    }

    @Operation(summary = "保存/修改步骤模板")
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody DietCookingStepPool entity) {
        if (entity == null) {
            return Result.failed("参数不能为空");
        }
        boolean success = cookingStepPoolService.saveStep(entity);
        return Result.success(success);
    }

    @Operation(summary = "删除步骤模板")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody BaseDeleteDTO dto) {
        boolean success = cookingStepPoolService.deleteSteps(dto);
        return Result.success(success);
    }
}

