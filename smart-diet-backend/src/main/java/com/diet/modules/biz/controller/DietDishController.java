package com.diet.modules.biz.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diet.modules.biz.model.dto.DietDishSaveDTO;
import com.diet.modules.biz.model.dto.DietDislikeDTO;
import com.diet.modules.biz.model.dto.DietSkilledDTO;
import com.diet.modules.biz.model.dto.DietWishDTO;
import com.diet.modules.biz.model.entity.DietDish;
import com.diet.modules.biz.model.entity.DietDishCookingBranch;
import com.diet.modules.biz.model.po.DietDishQueryPO;
import com.diet.modules.biz.model.vo.DietDishBranchVO;
import com.diet.modules.biz.model.vo.DietDishDetailVO;
import com.diet.modules.biz.model.vo.DietDishVO;
import com.diet.modules.biz.service.DietDishCookingBranchService;
import com.diet.modules.biz.service.DietDishService;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜谱管理 Controller 接口类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Tag(name = "业务管理-菜谱管理")
@RestController
@RequestMapping("/api/dish")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DietDishController {

    private final DietDishService dishService;
    private final DietDishCookingBranchService cookingBranchService;

    @Operation(summary = "查询做法分支列表(做法广场大卡片)")
    @GetMapping("/list-branches")
    public Result<List<DietDishBranchVO>> listBranches() {
        List<DietDishCookingBranch> branches = cookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDelFlag, 0)
                .list();
        if (CollUtil.isEmpty(branches)) {
            return Result.success(Collections.emptyList());
        }

        List<Long> dishIds = branches.stream().map(DietDishCookingBranch::getDishId).distinct().collect(Collectors.toList());
        Map<Long, DietDish> dishMap = dishService.listByIds(dishIds).stream()
                .collect(Collectors.toMap(DietDish::getDishId, d -> d));

        List<DietDishBranchVO> voList = branches.stream().map(b -> {
            DietDishBranchVO vo = new DietDishBranchVO();
            BeanUtil.copyProperties(b, vo);

            DietDish dish = dishMap.get(b.getDishId());
            if (Objects.nonNull(dish)) {
                vo.setDishId(dish.getDishId());
                vo.setDishName(dish.getDishName());
            }

            List<String> tags = new ArrayList<>();
            if (CharSequenceUtil.isNotBlank(b.getCuisineType())) {
                tags.add(b.getCuisineType());
            }
            if (b.getDietMode() != null) {
                if (b.getDietMode() == 0) tags.add("正常饮食");
                else if (b.getDietMode() == 1) tags.add("轻食减脂");
                else if (b.getDietMode() == 2) tags.add("放纵餐");
            }
            if (b.getProtein() != null && b.getProtein().doubleValue() > 10) {
                tags.add("高蛋白");
            }
            vo.setTags(tags);

            // 给定合理的默认初始值
            vo.setLikes(15);
            vo.setCollects(8);
            vo.setLiked(false);
            vo.setCollected(false);

            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "菜谱分页查询")
    @PostMapping("/page")
    public Result<List<DietDishVO>> pageDishes(@RequestBody DietDishQueryPO po) {
        IPage<DietDishVO> page = dishService.pageDishes(po);
        return Result.successPage(page);
    }

    @Operation(summary = "查询菜谱列表")
    @GetMapping("/list")
    public Result<List<DietDishVO>> listDishes(DietDishQueryPO po) {
        List<DietDishVO> list = dishService.listDishes(po);
        return Result.success(list);
    }

    @Operation(summary = "查询菜谱详情")
    @GetMapping("/detail")
    public Result<DietDishDetailVO> getDishDetail(@RequestParam Long dishId) {
        DietDishDetailVO detail = dishService.getDishDetail(dishId);
        return Result.success(detail);
    }

    @Operation(summary = "忌口反馈")
    @PostMapping("/dislike")
    public Result<Boolean> markDislike(@RequestBody DietDislikeDTO dto) {
        Boolean success = dishService.markDislike(dto);
        return Result.success(success);
    }

    @Operation(summary = "添加意向菜品")
    @PostMapping("/wish")
    public Result<Boolean> addWish(@RequestBody DietWishDTO dto) {
        Boolean success = dishService.addWish(dto);
        return Result.success(success);
    }

    @Operation(summary = "拿手菜标记管理")
    @PostMapping("/skilled")
    public Result<Boolean> manageSkilled(@RequestBody DietSkilledDTO dto) {
        Boolean success = dishService.manageSkilled(dto);
        return Result.success(success);
    }

    @Operation(summary = "保存/修改菜谱")
    @PostMapping("/save")
    public Result<Boolean> saveDish(@RequestBody DietDishSaveDTO dto) {
        Boolean success = dishService.saveDish(dto);
        return Result.success(success);
    }

    @Operation(summary = "删除菜谱")
    @PostMapping("/delete")
    public Result<Boolean> deleteDish(@RequestBody BaseDeleteDTO dto) {
        Boolean success = dishService.deleteDishes(dto);
        return Result.success(success);
    }

    @Operation(summary = "删除做法分支")
    @PostMapping("/delete/branch")
    public Result<Boolean> deleteBranch(@RequestBody BaseDeleteDTO dto) {
        Boolean success = dishService.deleteBranches(dto);
        return Result.success(success);
    }
}

