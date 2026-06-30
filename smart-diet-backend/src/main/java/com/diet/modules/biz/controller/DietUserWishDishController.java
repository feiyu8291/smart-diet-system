package com.diet.modules.biz.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.diet.modules.biz.model.dto.DietUserWishDishSaveDTO;
import com.diet.modules.biz.model.entity.DietDish;
import com.diet.modules.biz.model.entity.DietDishCookingBranch;
import com.diet.modules.biz.model.entity.DietUserWishDish;
import com.diet.modules.biz.model.vo.DietUserWishDishVO;
import com.diet.modules.biz.service.DietDishCookingBranchService;
import com.diet.modules.biz.service.DietDishService;
import com.diet.modules.biz.service.DietUserWishDishService;
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
import java.util.stream.Collectors;

/**
 * 成员心愿单管理 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Tag(name = "业务管理-成员心愿单管理")
@RestController
@RequestMapping("/api/diet/wish-dish")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietUserWishDishController {

    private final DietUserWishDishService wishDishService;
    private final DietDishService dishService;
    private final DietDishCookingBranchService cookingBranchService;

    @Operation(summary = "查询心愿单列表")
    @GetMapping("/list")
    public Result<List<DietUserWishDishVO>> listWishes(@RequestParam Long profileId) {
        List<DietUserWishDish> list = wishDishService.lambdaQuery()
                .eq(DietUserWishDish::getProfileId, profileId)
                .orderByDesc(DietUserWishDish::getCreateTime)
                .list();

        if (CollUtil.isEmpty(list)) {
            return Result.success(Collections.emptyList());
        }

        // 批量查询菜谱名
        List<Long> dishIds = list.stream().map(DietUserWishDish::getDishId).distinct().collect(Collectors.toList());
        Map<Long, String> dishMap = dishService.listByIds(dishIds).stream()
                .collect(Collectors.toMap(DietDish::getDishId, DietDish::getDishName));

        // 批量查询分支做法名
        List<Long> branchIds = list.stream()
                .map(DietUserWishDish::getBranchId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> branchMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(branchIds)) {
            branchMap = cookingBranchService.listByIds(branchIds).stream()
                    .collect(Collectors.toMap(DietDishCookingBranch::getBranchId, DietDishCookingBranch::getBranchName));
        }

        final Map<Long, String> finalBranchMap = branchMap;
        List<DietUserWishDishVO> voList = list.stream().map(item -> {
            DietUserWishDishVO vo = new DietUserWishDishVO();
            vo.setId(item.getWishId());
            vo.setProfileId(item.getProfileId());
            vo.setGroupId(item.getGroupId());
            vo.setDishId(item.getDishId());
            vo.setBranchId(item.getBranchId());
            vo.setWishDate(item.getWishDate() != null ? item.getWishDate().toString() : null);
            vo.setWishNote(item.getWishNote());

            // 拼装菜品与做法名字
            String dishName = dishMap.getOrDefault(item.getDishId(), "未知菜品");
            String branchName = finalBranchMap.get(item.getBranchId());
            if (CharSequenceUtil.isNotBlank(branchName)) {
                vo.setDishName(dishName + " (" + branchName + ")");
            } else {
                vo.setDishName(dishName);
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "添加心愿")
    @PostMapping("/add")
    public Result<Boolean> addWish(@RequestBody DietUserWishDishSaveDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getProfileId()) || Objects.isNull(dto.getDishId())) {
            return Result.success(false);
        }

        DietUserWishDish w = new DietUserWishDish();
        w.setProfileId(dto.getProfileId());
        w.setGroupId(dto.getGroupId());
        w.setDishId(dto.getDishId());
        w.setBranchId(dto.getBranchId());
        if (CharSequenceUtil.isNotBlank(dto.getWishDate())) {
            w.setWishDate(LocalDate.parse(dto.getWishDate()));
        }
        w.setWishNote(dto.getWishNote());
        w.setDelFlag(0);

        boolean success = wishDishService.save(w);
        return Result.success(success);
    }

    @Operation(summary = "撤销心愿")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteWish(@PathVariable("id") Long id) {
        boolean success = wishDishService.removeById(id);
        return Result.success(success);
    }
}
