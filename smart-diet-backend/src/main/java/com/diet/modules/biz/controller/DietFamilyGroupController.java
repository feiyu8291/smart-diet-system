package com.diet.modules.biz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diet.modules.biz.model.entity.DietFamilyGroup;
import com.diet.modules.biz.model.po.DietFamilyGroupQueryPO;
import com.diet.modules.biz.model.vo.DietFamilyGroupVO;
import com.diet.modules.biz.service.DietFamilyGroupService;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 家庭分组 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-21
 */
@Tag(name = "业务管理-家庭分组")
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietFamilyGroupController {

    private final DietFamilyGroupService familyGroupService;

    @Operation(summary = "家庭分组分页查询")
    @PostMapping("/page")
    public Result<List<DietFamilyGroupVO>> page(@RequestBody DietFamilyGroupQueryPO po) {
        IPage<DietFamilyGroupVO> pageResult = familyGroupService.pageGroups(po);
        return Result.successPage(pageResult);
    }

    @Operation(summary = "查询家庭分组列表")
    @GetMapping("/list")
    public Result<List<DietFamilyGroupVO>> list(@RequestParam(required = false) Long userId) {
        List<DietFamilyGroupVO> list = familyGroupService.listGroups(userId);
        return Result.success(list);
    }

    @Operation(summary = "保存/修改家庭分组")
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody DietFamilyGroup entity) {
        Boolean success = familyGroupService.saveGroup(entity);
        return Result.success(success);
    }

    @Operation(summary = "软删除家庭分组")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody BaseDeleteDTO dto) {
        Boolean success = familyGroupService.deleteGroups(dto);
        return Result.success(success);
    }
}

