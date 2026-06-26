package com.diet.modules.biz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diet.modules.biz.model.dto.DietUserHealthProfileDTO;
import com.diet.modules.biz.model.po.DietUserHealthProfileQueryPO;
import com.diet.modules.biz.model.vo.DietUserHealthProfileVO;
import com.diet.modules.biz.service.DietUserHealthProfileService;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成员健康档案 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "业务管理-成员健康档案")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietUserHealthProfileController {

    private final DietUserHealthProfileService userHealthProfileService;

    @Operation(summary = "查询家庭成员健康档案列表")
    @GetMapping("/list")
    public Result<List<DietUserHealthProfileVO>> list(@RequestParam(required = false) Long groupId) {
        List<DietUserHealthProfileVO> list = userHealthProfileService.listProfiles(groupId);
        return Result.success(list);
    }

    @Operation(summary = "查询家庭成员健康档案分页列表")
    @GetMapping("/page")
    public Result<List<DietUserHealthProfileVO>> page(DietUserHealthProfileQueryPO po) {
        IPage<DietUserHealthProfileVO> page = userHealthProfileService.pageProfiles(po);
        return Result.successPage(page);
    }

    @Operation(summary = "保存/修改成员档案")
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody DietUserHealthProfileDTO dto) {
        Boolean success = userHealthProfileService.saveProfile(dto);
        return Result.success(success);
    }

    @Operation(summary = "软删除成员档案")
    @DeleteMapping("/delete/{profileId}")
    public Result<Boolean> delete(@PathVariable Long profileId) {
        Boolean success = userHealthProfileService.deleteProfile(profileId);
        return Result.success(success);
    }
}
