package com.diet.modules.biz.controller;

import cn.hutool.core.collection.CollUtil;
import com.diet.modules.biz.model.dto.DietDislikeDTO;
import com.diet.modules.biz.model.dto.DietUserDislikeDishSaveDTO;
import com.diet.modules.biz.model.entity.DietDish;
import com.diet.modules.biz.model.entity.DietUserDislikeDish;
import com.diet.modules.biz.model.vo.DietUserDislikeDishVO;
import com.diet.modules.biz.service.DietDishService;
import com.diet.modules.biz.service.DietUserDislikeDishService;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 成员不喜欢/避坑菜品管理 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Tag(name = "业务管理-成员避坑单管理")
@RestController
@RequestMapping("/api/diet/dislike-dish")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietUserDislikeDishController {

    private final DietUserDislikeDishService dislikeDishService;
    private final DietDishService dishService;

    @Operation(summary = "查询避坑单列表")
    @GetMapping("/list")
    public Result<List<DietUserDislikeDishVO>> listDislikes(@RequestParam Long profileId) {
        List<DietUserDislikeDish> list = dislikeDishService.lambdaQuery()
                .eq(DietUserDislikeDish::getProfileId, profileId)
                .orderByDesc(DietUserDislikeDish::getUpdateTime)
                .list();

        if (CollUtil.isEmpty(list)) {
            return Result.success(Collections.emptyList());
        }

        // 批量查询菜谱名
        List<Long> dishIds = list.stream().map(DietUserDislikeDish::getDishId).distinct().collect(Collectors.toList());
        Map<Long, String> dishMap = dishService.listByIds(dishIds).stream()
                .collect(Collectors.toMap(DietDish::getDishId, DietDish::getDishName));

        List<DietUserDislikeDishVO> voList = list.stream().map(item -> {
            DietUserDislikeDishVO vo = new DietUserDislikeDishVO();
            vo.setId(item.getDislikeId());
            vo.setProfileId(item.getProfileId());
            vo.setGroupId(item.getGroupId());
            vo.setDishId(item.getDishId());
            vo.setCount(item.getDislikeCount());
            vo.setDishName(dishMap.getOrDefault(item.getDishId(), "未知菜品"));
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "申报不喜欢/避坑菜品")
    @PostMapping("/add")
    public Result<Boolean> addDislike(@RequestBody DietUserDislikeDishSaveDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getProfileId()) || Objects.isNull(dto.getDishId())) {
            return Result.success(false);
        }

        // 复用已有 Service 层的增加/吐槽次数递增逻辑
        DietDislikeDTO serviceDto = new DietDislikeDTO();
        serviceDto.setProfileId(dto.getProfileId());
        serviceDto.setGroupId(dto.getGroupId());
        serviceDto.setDishId(dto.getDishId());

        Boolean success = dishService.markDislike(serviceDto);
        return Result.success(success);
    }

    @Operation(summary = "移出避坑单")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteDislike(@PathVariable("id") Long id) {
        boolean success = dislikeDishService.removeById(id);
        return Result.success(success);
    }
}
