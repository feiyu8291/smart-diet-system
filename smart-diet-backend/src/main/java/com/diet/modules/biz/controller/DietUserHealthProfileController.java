package com.diet.modules.biz.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diet.modules.biz.model.dto.DietUserHealthProfileDTO;
import com.diet.modules.biz.model.entity.DietFamilyGroup;
import com.diet.modules.biz.model.entity.DietUserHealthProfile;
import com.diet.modules.biz.model.po.DietUserHealthProfileQueryPO;
import com.diet.modules.biz.model.vo.DietUserHealthProfileVO;
import com.diet.modules.biz.service.DietFamilyGroupService;
import com.diet.modules.biz.service.DietUserHealthProfileService;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
    private final DietFamilyGroupService familyGroupService;

    @Operation(summary = "查询单个成员健康档案详情")
    @GetMapping("/{profileId}")
    public Result<DietUserHealthProfileVO> getProfileById(@PathVariable("profileId") Long profileId) {
        DietUserHealthProfile profile = userHealthProfileService.getById(profileId);
        if (Objects.isNull(profile)) {
            return Result.success(null);
        }
        DietUserHealthProfileVO vo = new DietUserHealthProfileVO();
        BeanUtil.copyProperties(profile, vo);
        if (Objects.nonNull(profile.getGroupId())) {
            DietFamilyGroup group = familyGroupService.getById(profile.getGroupId());
            if (Objects.nonNull(group)) {
                vo.setGroupName(group.getGroupName());
            }
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询当前用户关联成员档案")
    @GetMapping("/my-profile")
    public Result<DietUserHealthProfileVO> getMyProfile() {
        Long userId = com.diet.modules.common.util.SecurityUtils.getCurrentUserId();
        DietUserHealthProfile profile = userHealthProfileService.lambdaQuery()
                .eq(DietUserHealthProfile::getUserId, userId)
                .eq(DietUserHealthProfile::getDelFlag, 0)
                .last("LIMIT 1")
                .one();
        if (Objects.isNull(profile)) {
            return Result.success(null);
        }
        DietUserHealthProfileVO vo = new DietUserHealthProfileVO();
        BeanUtil.copyProperties(profile, vo);
        if (Objects.nonNull(profile.getGroupId())) {
            DietFamilyGroup group = familyGroupService.getById(profile.getGroupId());
            if (Objects.nonNull(group)) {
                vo.setGroupName(group.getGroupName());
            }
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询家庭成员健康档案分页列表")
    @PostMapping("/page")
    public Result<List<DietUserHealthProfileVO>> page(@RequestBody DietUserHealthProfileQueryPO po) {
        IPage<DietUserHealthProfileVO> page = userHealthProfileService.pageProfiles(po);
        return Result.successPage(page);
    }

    @Operation(summary = "查询家庭成员健康档案列表")
    @GetMapping("/list")
    public Result<List<DietUserHealthProfileVO>> list(@RequestParam(required = false) Long groupId) {
        List<DietUserHealthProfileVO> list = userHealthProfileService.listProfiles(groupId);
        return Result.success(list);
    }

    @Operation(summary = "保存/修改成员档案")
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody DietUserHealthProfileDTO dto) {
        Boolean success = userHealthProfileService.saveProfile(dto);
        return Result.success(success);
    }

    @Operation(summary = "软删除成员档案")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody BaseDeleteDTO dto) {
        Boolean success = userHealthProfileService.deleteProfiles(dto);
        return Result.success(success);
    }
}

