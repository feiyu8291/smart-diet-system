package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.*;
import com.diet.modules.biz.model.dto.*;
import com.diet.modules.biz.model.entity.*;
import com.diet.modules.biz.model.po.DietDishQueryPO;
import com.diet.modules.biz.model.vo.*;
import com.diet.modules.common.aspect.DictData;
import com.diet.modules.system.mapper.SysFileStorageMapper;
import com.diet.modules.system.model.entity.SysFileStorage;
import com.diet.modules.system.service.SysDataDictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜谱管理业务服务类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DietDishService extends ServiceImpl<DietDishMapper, DietDish> {

    private final DietDishMapper dishMapper;
    private final DietDishIngredientMapper dishIngredientMapper;
    private final DietIngredientMapper ingredientMapper;
    private final DietDishStepRelationMapper dishStepRelationMapper;
    private final DietCookingStepPoolMapper cookingStepPoolMapper;
    private final DietCookDishStatMapper cookDishStatMapper;
    private final DietCookSkilledDishMapper cookSkilledDishMapper;
    private final DietUserWishDishMapper userWishDishMapper;
    private final DietUserDislikeDishMapper userDislikeDishMapper;
    private final DietUserDishImageMapper userDishImageMapper;
    private final SysFileStorageMapper sysFileStorageMapper;
    private final SysDataDictionaryService sysDataDictionaryService;
    private final DietDishCookingBranchMapper dishCookingBranchMapper;

    /**
     * 获取所有有效的菜谱及对应的熟练度与图片
     */
    public List<DietDishVO> listDishes(DietDishQueryPO po) {
        // 1. 获取所有有效的菜谱
        LambdaQueryWrapper<DietDish> query = new LambdaQueryWrapper<>();
        query.eq(DietDish::getDelFlag, 0);
        List<DietDish> dishes = dishMapper.selectList(query);

        // 2. 提前查询当前做饭人（userId）的统计信息
        Map<Long, DietCookDishStat> statMap = new HashMap<>();
        Set<Long> skilledDishIds = new HashSet<>();
        if (po.getUserId() != null) {
            LambdaQueryWrapper<DietCookDishStat> statQuery = new LambdaQueryWrapper<>();
            statQuery.eq(DietCookDishStat::getUserId, po.getUserId()).eq(DietCookDishStat::getDelFlag, 0);
            List<DietCookDishStat> stats = cookDishStatMapper.selectList(statQuery);
            for (DietCookDishStat stat : stats) {
                statMap.put(stat.getDishId(), stat);
            }

            LambdaQueryWrapper<DietCookSkilledDish> skilledQuery = new LambdaQueryWrapper<>();
            skilledQuery.eq(DietCookSkilledDish::getUserId, po.getUserId()).eq(DietCookSkilledDish::getDelFlag, 0);
            List<DietCookSkilledDish> skilled = cookSkilledDishMapper.selectList(skilledQuery);
            skilledDishIds = skilled.stream().map(DietCookSkilledDish::getDishId).collect(Collectors.toSet());
        }

        // 3. 提前查询自制成品图覆盖信息（按家庭组 groupId）
        Map<Long, String> customImageMap = new HashMap<>();
        if (po.getGroupId() != null) {
            LambdaQueryWrapper<DietUserDishImage> imgQuery = new LambdaQueryWrapper<>();
            imgQuery.eq(DietUserDishImage::getGroupId, po.getGroupId()).eq(DietUserDishImage::getDelFlag, 0);
            List<DietUserDishImage> userImages = userDishImageMapper.selectList(imgQuery);
            for (DietUserDishImage userImg : userImages) {
                SysFileStorage fs = sysFileStorageMapper.selectById(userImg.getStorageId());
                if (fs != null && fs.getDelFlag() == 0) {
                    customImageMap.put(userImg.getDishId(), fs.getFilePath());
                }
            }
        }

        // 4. 组装复合数据 VO
        List<DietDishVO> response = new ArrayList<>();
        for (DietDish dish : dishes) {
            DietDishVO vo = new DietDishVO();
            BeanUtil.copyProperties(dish, vo);

            // 图片预览逻辑：优先展现该家庭组 MinIO 的自制成品图，其次展示系统默认图
            String previewUrl = customImageMap.get(dish.getDishId());
            if (previewUrl == null && dish.getCoverImageId() != null) {
                SysFileStorage defaultImg = sysFileStorageMapper.selectById(dish.getCoverImageId());
                if (defaultImg != null && defaultImg.getDelFlag() == 0) {
                    previewUrl = defaultImg.getFilePath();
                }
            }
            vo.setPreviewUrl(previewUrl);

            // 做饭人的统计熟练度
            DietCookDishStat stat = statMap.get(dish.getDishId());
            vo.setCookCount(stat != null ? stat.getCookCount() : 0);
            vo.setSignatureFlag(stat != null ? stat.getSignatureFlag() : 0);
            vo.setIsSkilled(skilledDishIds.contains(dish.getDishId()) ? 1 : 0);

            response.add(vo);
        }

        return response;
    }

    /**
     * 分页查询菜谱列表及做饭人统计信息
     */
    @DictData
    public IPage<DietDishVO> pageDishes(DietDishQueryPO po) {
        Page<DietDishVO> page = new Page<>(po.getPageNo(), po.getPageSize());
        IPage<DietDishVO> result = dishMapper.selectDishPage(page, po);
        List<DietDishVO> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            for (DietDishVO vo : records) {
                // 为了兼容前端列表做法Tag的渲染，为其组装只包含当前分支自身的单元素列表
                List<DietDishBranchVO> branchVOList = new ArrayList<>();
                DietDishBranchVO branchVO = new DietDishBranchVO();
                branchVO.setBranchId(vo.getBranchId());
                branchVO.setBranchName(vo.getBranchName());
                branchVO.setCuisineType(vo.getCuisineType());
                branchVO.setDietMode(vo.getDietMode() != null ? Integer.parseInt(vo.getDietMode()) : 0);
                branchVO.setCalories(vo.getCalories() != null ? BigDecimal.valueOf(vo.getCalories()) : BigDecimal.ZERO);
                branchVO.setProtein(vo.getProtein());
                branchVO.setFat(vo.getFat());
                branchVO.setCarbs(vo.getCarbs());
                branchVOList.add(branchVO);
                vo.setBranches(branchVOList);
            }
        }
        return result;
    }

    /**
     * 获取指定菜谱的详情信息 (配料原料列表和做法步骤列表)
     */
    public DietDishDetailVO getDishDetail(Long dishId) {
        DietDish dish = dishMapper.selectById(dishId);
        if (dish == null || dish.getDelFlag() == 1) {
            throw new RuntimeException("菜品不存在");
        }

        DietDishDetailVO detailVO = new DietDishDetailVO();
        detailVO.setDish(dish);

        // 查找该菜品所有有效的做法分支
        LambdaQueryWrapper<DietDishCookingBranch> branchQuery = new LambdaQueryWrapper<>();
        branchQuery.eq(DietDishCookingBranch::getDishId, dishId).eq(DietDishCookingBranch::getDelFlag, 0);
        List<DietDishCookingBranch> branches = dishCookingBranchMapper.selectList(branchQuery);

        // 如果做法分支为空，为向下兼容，自动创建一个默认分支并将原有关联的数据绑定到该分支上
        if (branches.isEmpty()) {
            DietDishCookingBranch defaultBranch = new DietDishCookingBranch();
            defaultBranch.setDishId(dishId);
            defaultBranch.setBranchName("经典做法");
            defaultBranch.setCuisineType("粤菜");
            defaultBranch.setDietMode(0);
            defaultBranch.setCalories(BigDecimal.ZERO);
            defaultBranch.setProtein(BigDecimal.ZERO);
            defaultBranch.setFat(BigDecimal.ZERO);
            defaultBranch.setCarbs(BigDecimal.ZERO);
            defaultBranch.setDelFlag(0);
            defaultBranch.setCreateTime(LocalDateTime.now());
            defaultBranch.setUpdateTime(LocalDateTime.now());
            dishCookingBranchMapper.insert(defaultBranch);

            Long defaultBranchId = defaultBranch.getBranchId();

            // 绑定已有的原料和步骤关联关系到此做法分支上
            DietDishIngredient updateIng = new DietDishIngredient();
            updateIng.setBranchId(defaultBranchId);
            LambdaQueryWrapper<DietDishIngredient> ingUpdateWrapper = new LambdaQueryWrapper<>();
            ingUpdateWrapper.eq(DietDishIngredient::getDishId, dishId);
            dishIngredientMapper.update(updateIng, ingUpdateWrapper);

            DietDishStepRelation updateStep = new DietDishStepRelation();
            updateStep.setBranchId(defaultBranchId);
            LambdaQueryWrapper<DietDishStepRelation> stepUpdateWrapper = new LambdaQueryWrapper<>();
            stepUpdateWrapper.eq(DietDishStepRelation::getDishId, dishId);
            dishStepRelationMapper.update(updateStep, stepUpdateWrapper);

            branches = Collections.singletonList(defaultBranch);
        }

        List<DietDishBranchVO> branchVOList = new ArrayList<>();
        for (DietDishCookingBranch branch : branches) {
            DietDishBranchVO branchVO = new DietDishBranchVO();
            branchVO.setBranchId(branch.getBranchId());
            branchVO.setBranchName(branch.getBranchName());
            branchVO.setCuisineType(branch.getCuisineType());
            branchVO.setDietMode(branch.getDietMode());
            branchVO.setCalories(branch.getCalories());
            branchVO.setProtein(branch.getProtein());
            branchVO.setFat(branch.getFat());
            branchVO.setCarbs(branch.getCarbs());
            branchVO.setCoverImageId(branch.getCoverImageId());
            String previewUrl = null;
            if (branch.getCoverImageId() != null) {
                SysFileStorage img = sysFileStorageMapper.selectById(branch.getCoverImageId());
                if (img != null && img.getDelFlag() == 0) {
                    previewUrl = img.getFilePath();
                }
            }
            branchVO.setPreviewUrl(previewUrl);

            // 1. 获取配方原料列表 (dish_ingredient)
            LambdaQueryWrapper<DietDishIngredient> ingredQuery = new LambdaQueryWrapper<>();
            ingredQuery.eq(DietDishIngredient::getDishId, dishId)
                    .eq(DietDishIngredient::getBranchId, branch.getBranchId())
                    .eq(DietDishIngredient::getDelFlag, 0);
            List<DietDishIngredient> rels = dishIngredientMapper.selectList(ingredQuery);

            List<DietDishIngredientVO> ingredients = new ArrayList<>();
            for (DietDishIngredient rel : rels) {
                DietDishIngredientVO item = new DietDishIngredientVO();
                item.setRelationId(rel.getRelationId());
                item.setUseAmount(rel.getUseAmount() != null ? rel.getUseAmount().toString() : null);
                item.setMainMaterialFlag(rel.getMainMaterialFlag());

                DietIngredient ing = ingredientMapper.selectById(rel.getIngredientId());
                if (ing != null) {
                    item.setIngredientId(ing.getIngredientId());
                    item.setIngredientName(ing.getIngredientName());
                    item.setMeasureUnit(ing.getMeasureUnit());
                }
                ingredients.add(item);
            }
            branchVO.setIngredients(ingredients);

            // 2. 获取做法步骤列表 (dish_step_relation)
            LambdaQueryWrapper<DietDishStepRelation> stepQuery = new LambdaQueryWrapper<>();
            stepQuery.eq(DietDishStepRelation::getDishId, dishId)
                    .eq(DietDishStepRelation::getBranchId, branch.getBranchId())
                    .eq(DietDishStepRelation::getDelFlag, 0)
                    .orderByAsc(DietDishStepRelation::getStepNum);
            List<DietDishStepRelation> stepRels = dishStepRelationMapper.selectList(stepQuery);

            List<DietDishStepVO> steps = new ArrayList<>();
            for (DietDishStepRelation rel : stepRels) {
                DietDishStepVO item = new DietDishStepVO();
                item.setStepNum(rel.getStepNum());

                String detail = rel.getCustomDetail();
                if (detail == null || detail.trim().isEmpty()) {
                    DietCookingStepPool pool = cookingStepPoolMapper.selectById(rel.getStepPoolId());
                    if (pool != null) {
                        detail = pool.getStepDetail();
                    }
                }
                item.setStepDetail(detail);
                item.setDurationSeconds(rel.getDurationSeconds());
                item.setFirePower(rel.getFirePower());
                if (rel.getFirePower() != null) {
                    item.setFirePowerLabel(sysDataDictionaryService.selectValueByTypeAndCode("fire_power_type", rel.getFirePower().toString()));
                } else {
                    item.setFirePowerLabel("");
                }
                steps.add(item);
            }
            branchVO.setSteps(steps);

            branchVOList.add(branchVO);
        }
        detailVO.setBranches(branchVOList);

        // 为了老版本的接口调用向下兼容，根级结构默认填入首个做法分支的数据
        if (!branchVOList.isEmpty()) {
            detailVO.setIngredients(branchVOList.get(0).getIngredients());
            detailVO.setSteps(branchVOList.get(0).getSteps());
        }

        return detailVO;
    }

    /**
     * 标记普通家庭成员对特定菜谱表示的忌口或讨厌反馈
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean markDislike(DietDislikeDTO dto) {
        if (dto == null || dto.getProfileId() == null || dto.getGroupId() == null || dto.getDishId() == null) {
            return false;
        }

        LambdaQueryWrapper<DietUserDislikeDish> query = new LambdaQueryWrapper<>();
        query.eq(DietUserDislikeDish::getProfileId, dto.getProfileId())
                .eq(DietUserDislikeDish::getDishId, dto.getDishId());
        DietUserDislikeDish existing = userDislikeDishMapper.selectOne(query);

        if (existing != null) {
            existing.setDislikeCount(existing.getDislikeCount() + 1);
            existing.setUpdateTime(LocalDateTime.now());
            return userDislikeDishMapper.updateById(existing) > 0;
        } else {
            DietUserDislikeDish d = new DietUserDislikeDish();
            d.setProfileId(dto.getProfileId());
            d.setGroupId(dto.getGroupId());
            d.setDishId(dto.getDishId());
            d.setDislikeCount(1);
            d.setDelFlag(0);
            return userDislikeDishMapper.insert(d) > 0;
        }
    }

    /**
     * 添加意向菜品计划
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean addWish(DietWishDTO dto) {
        if (dto == null || dto.getProfileId() == null || dto.getGroupId() == null || dto.getDishId() == null) {
            return false;
        }

        DietUserWishDish w = new DietUserWishDish();
        w.setProfileId(dto.getProfileId());
        w.setGroupId(dto.getGroupId());
        w.setDishId(dto.getDishId());
        if (dto.getWishDate() != null && !dto.getWishDate().trim().isEmpty()) {
            w.setWishDate(LocalDate.parse(dto.getWishDate()));
        }
        w.setDelFlag(0);
        return userWishDishMapper.insert(w) > 0;
    }

    /**
     * 添加或取消拿手菜/擅长标记
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean manageSkilled(DietSkilledDTO dto) {
        if (dto == null || dto.getUserId() == null || dto.getDishId() == null || dto.getIsSkilled() == null) {
            return false;
        }

        LambdaQueryWrapper<DietCookSkilledDish> query = new LambdaQueryWrapper<>();
        query.eq(DietCookSkilledDish::getUserId, dto.getUserId())
                .eq(DietCookSkilledDish::getDishId, dto.getDishId());
        DietCookSkilledDish existing = cookSkilledDishMapper.selectOne(query);

        if (dto.getIsSkilled() == 1) { // 添加擅长
            if (existing == null) {
                DietCookSkilledDish skill = new DietCookSkilledDish();
                skill.setUserId(dto.getUserId());
                skill.setDishId(dto.getDishId());
                skill.setDelFlag(0);
                return cookSkilledDishMapper.insert(skill) > 0;
            } else if (existing.getDelFlag() == 1) {
                existing.setDelFlag(0);
                existing.setUpdateTime(LocalDateTime.now());
                return cookSkilledDishMapper.updateById(existing) > 0;
            }
            return true;
        } else { // 移除擅长
            if (existing != null && existing.getDelFlag() == 0) {
                existing.setDelFlag(1); // 软删除
                existing.setUpdateTime(LocalDateTime.now());
                return cookSkilledDishMapper.updateById(existing) > 0;
            }
            return true;
        }
    }

    /**
     * 保存或更新菜谱，包含其配料与步骤关联
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveDish(com.diet.modules.biz.model.dto.DietDishSaveDTO dto) {
        if (dto == null || dto.getDishName() == null || dto.getDishName().trim().isEmpty()) {
            return false;
        }

        // 1. 组装并保存菜品主表
        DietDish dish = new DietDish();
        if (dto.getDishId() != null) {
            dish = dishMapper.selectById(dto.getDishId());
            if (dish == null) {
                throw new RuntimeException("修改的菜品不存在！");
            }
        } else {
            dish.setDelFlag(0);
            dish.setCreateTime(LocalDateTime.now());
        }

        dish.setDishName(dto.getDishName());
        dish.setCoverImageId(dto.getCoverImageId());
        dish.setUpdateTime(LocalDateTime.now());

        // 保存或修改主表
        if (dto.getDishId() != null) {
            dishMapper.updateById(dish);
        } else {
            dishMapper.insert(dish);
        }
        Long dishId = dish.getDishId();

        // 2. 解析分支做法数据（如果 branches 为空，默认创建一个做法分支）
        List<DietDishBranchSaveDTO> branches = dto.getBranches();
        if (branches == null || branches.isEmpty()) {
            DietDishBranchSaveDTO defaultBranch = new DietDishBranchSaveDTO();
            defaultBranch.setBranchName("经典做法");
            defaultBranch.setCuisineType("粤菜");
            defaultBranch.setDietMode(0);
            defaultBranch.setAutoCalculateNutrients(true);
            defaultBranch.setCalories(BigDecimal.ZERO);
            defaultBranch.setProtein(BigDecimal.ZERO);
            defaultBranch.setFat(BigDecimal.ZERO);
            defaultBranch.setCarbs(BigDecimal.ZERO);
            defaultBranch.setIngredients(dto.getIngredients());
            defaultBranch.setSteps(dto.getSteps());
            branches = Collections.singletonList(defaultBranch);
        }

        // 3. 对比已有的分支数据，清理已被用户删除的做法分支及其配料与步骤关联
        Set<Long> incomingBranchIds = new HashSet<>();
        for (DietDishBranchSaveDTO branchDTO : branches) {
            if (branchDTO.getBranchId() != null) {
                incomingBranchIds.add(branchDTO.getBranchId());
            }
        }

        LambdaQueryWrapper<DietDishCookingBranch> branchDelQuery = new LambdaQueryWrapper<>();
        branchDelQuery.eq(DietDishCookingBranch::getDishId, dishId);
        if (!incomingBranchIds.isEmpty()) {
            branchDelQuery.notIn(DietDishCookingBranch::getBranchId, incomingBranchIds);
        }
        List<DietDishCookingBranch> toDeleteBranches = dishCookingBranchMapper.selectList(branchDelQuery);
        for (DietDishCookingBranch b : toDeleteBranches) {
            b.setDelFlag(1);
            b.setUpdateTime(LocalDateTime.now());
            dishCookingBranchMapper.updateById(b);

            LambdaQueryWrapper<DietDishIngredient> ingClear = new LambdaQueryWrapper<>();
            ingClear.eq(DietDishIngredient::getBranchId, b.getBranchId());
            dishIngredientMapper.delete(ingClear);

            LambdaQueryWrapper<DietDishStepRelation> stepClear = new LambdaQueryWrapper<>();
            stepClear.eq(DietDishStepRelation::getBranchId, b.getBranchId());
            dishStepRelationMapper.delete(stepClear);
        }

        // 4. 保存/更新每个做法分支，并处理其关联的原材料配料与步骤
        List<DietDishCookingBranch> savedBranches = new ArrayList<>();
        for (DietDishBranchSaveDTO branchDTO : branches) {
            DietDishCookingBranch branch = new DietDishCookingBranch();
            if (branchDTO.getBranchId() != null) {
                branch = dishCookingBranchMapper.selectById(branchDTO.getBranchId());
                if (branch == null) {
                    throw new RuntimeException("修改的做法分支不存在！");
                }
            } else {
                branch.setDishId(dishId);
                branch.setDelFlag(0);
                branch.setCreateTime(LocalDateTime.now());
            }

            branch.setBranchName(branchDTO.getBranchName());
            branch.setCuisineType(branchDTO.getCuisineType() != null ? branchDTO.getCuisineType() : "粤菜");
            branch.setDietMode(branchDTO.getDietMode() != null ? branchDTO.getDietMode() : 0);
            branch.setCoverImageId(branchDTO.getCoverImageId());
            branch.setUpdateTime(LocalDateTime.now());

            // 智能核算热量与三大营养素
            boolean needsCalc = (branchDTO.getAutoCalculateNutrients() != null && branchDTO.getAutoCalculateNutrients())
                    || (branchDTO.getCalories() == null || branchDTO.getCalories().doubleValue() <= 0);

            if (!needsCalc) {
                branch.setCalories(branchDTO.getCalories());
                branch.setProtein(branchDTO.getProtein() != null ? branchDTO.getProtein() : BigDecimal.ZERO);
                branch.setFat(branchDTO.getFat() != null ? branchDTO.getFat() : BigDecimal.ZERO);
                branch.setCarbs(branchDTO.getCarbs() != null ? branchDTO.getCarbs() : BigDecimal.ZERO);
            } else {
                if (branchDTO.getIngredients() == null || branchDTO.getIngredients().isEmpty()) {
                    branch.setCalories(BigDecimal.ZERO);
                    branch.setProtein(BigDecimal.ZERO);
                    branch.setFat(BigDecimal.ZERO);
                    branch.setCarbs(BigDecimal.ZERO);
                } else {
                    double totalWeight = 0;
                    double totalCals = 0;
                    double totalProt = 0;
                    double totalFat = 0;
                    double totalCarbs = 0;

                    for (DietDishIngredientSaveDTO ingDto : branchDTO.getIngredients()) {
                        DietIngredient ing = ingredientMapper.selectById(ingDto.getIngredientId());
                        if (ing != null && ing.getDelFlag() == 0 && ingDto.getUseAmount() != null) {
                            double weight = ingDto.getUseAmount().doubleValue();
                            totalWeight += weight;
                            totalCals += (ing.getCalories().doubleValue() * weight) / 100.0;
                            totalProt += (ing.getProtein().doubleValue() * weight) / 100.0;
                            totalFat += (ing.getFat().doubleValue() * weight) / 100.0;
                            totalCarbs += (ing.getCarbs().doubleValue() * weight) / 100.0;
                        }
                    }

                    if (totalWeight > 0) {
                        branch.setCalories(BigDecimal.valueOf((totalCals / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
                        branch.setProtein(BigDecimal.valueOf((totalProt / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
                        branch.setFat(BigDecimal.valueOf((totalFat / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
                        branch.setCarbs(BigDecimal.valueOf((totalCarbs / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
                    } else {
                        branch.setCalories(BigDecimal.ZERO);
                        branch.setProtein(BigDecimal.ZERO);
                        branch.setFat(BigDecimal.ZERO);
                        branch.setCarbs(BigDecimal.ZERO);
                    }
                }
            }

            if (branch.getBranchId() != null) {
                dishCookingBranchMapper.updateById(branch);
            } else {
                dishCookingBranchMapper.insert(branch);
            }
            Long branchId = branch.getBranchId();
            savedBranches.add(branch);

            // 清理此做法分支已有的配料与步骤关联
            LambdaQueryWrapper<DietDishIngredient> ingDelete = new LambdaQueryWrapper<>();
            ingDelete.eq(DietDishIngredient::getBranchId, branchId);
            dishIngredientMapper.delete(ingDelete);

            LambdaQueryWrapper<DietDishStepRelation> stepDelete = new LambdaQueryWrapper<>();
            stepDelete.eq(DietDishStepRelation::getBranchId, branchId);
            dishStepRelationMapper.delete(stepDelete);

            // 插入配料关系
            if (branchDTO.getIngredients() != null) {
                for (DietDishIngredientSaveDTO ingDto : branchDTO.getIngredients()) {
                    DietDishIngredient rel = new DietDishIngredient();
                    rel.setDishId(dishId);
                    rel.setBranchId(branchId);
                    rel.setIngredientId(ingDto.getIngredientId());
                    rel.setUseAmount(ingDto.getUseAmount());
                    rel.setMainMaterialFlag(ingDto.getMainMaterialFlag() != null ? ingDto.getMainMaterialFlag() : 1);
                    rel.setDelFlag(0);
                    rel.setCreateTime(LocalDateTime.now());
                    rel.setUpdateTime(LocalDateTime.now());
                    dishIngredientMapper.insert(rel);
                }
            }

            // 插入步骤关系
            if (branchDTO.getSteps() != null) {
                for (DietDishStepSaveDTO stepDto : branchDTO.getSteps()) {
                    DietDishStepRelation rel = new DietDishStepRelation();
                    rel.setDishId(dishId);
                    rel.setBranchId(branchId);
                    rel.setStepPoolId(stepDto.getStepPoolId() != null ? stepDto.getStepPoolId() : 0L);
                    rel.setStepNum(stepDto.getStepNum());
                    rel.setCustomDetail(stepDto.getCustomDetail());
                    rel.setDurationSeconds(stepDto.getDurationSeconds() != null ? stepDto.getDurationSeconds() : 0);
                    rel.setFirePower(stepDto.getFirePower() != null ? stepDto.getFirePower() : 0);
                    rel.setDelFlag(0);
                    rel.setCreateTime(LocalDateTime.now());
                    rel.setUpdateTime(LocalDateTime.now());
                    dishStepRelationMapper.insert(rel);
                }
            }
        }

        return true;
    }

    /**
     * 软删除特定做法分支，级联清理其配料及步骤关联。如果该菜谱已无其他有效分支，则同步软删除菜谱主表。
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBranch(Long branchId) {
        DietDishCookingBranch branch = dishCookingBranchMapper.selectById(branchId);
        if (branch == null || branch.getDelFlag() == 1) {
            return false;
        }

        // 1. 软删除此做法分支
        branch.setDelFlag(1);
        branch.setUpdateTime(LocalDateTime.now());
        int updated = dishCookingBranchMapper.updateById(branch);
        if (updated > 0) {
            // 2. 清除配料及步骤关联
            LambdaQueryWrapper<DietDishIngredient> ingClear = new LambdaQueryWrapper<>();
            ingClear.eq(DietDishIngredient::getBranchId, branchId);
            dishIngredientMapper.delete(ingClear);

            LambdaQueryWrapper<DietDishStepRelation> stepClear = new LambdaQueryWrapper<>();
            stepClear.eq(DietDishStepRelation::getBranchId, branchId);
            dishStepRelationMapper.delete(stepClear);

            // 3. 检查主菜谱下是否还存在其他有效的做法分支。若无，级联删除主菜谱记录
            LambdaQueryWrapper<DietDishCookingBranch> activeBranchesQuery = new LambdaQueryWrapper<>();
            activeBranchesQuery.eq(DietDishCookingBranch::getDishId, branch.getDishId())
                    .eq(DietDishCookingBranch::getDelFlag, 0);
            Long activeCount = dishCookingBranchMapper.selectCount(activeBranchesQuery);
            if (activeCount == 0) {
                DietDish dish = dishMapper.selectById(branch.getDishId());
                if (dish != null && dish.getDelFlag() == 0) {
                    dish.setDelFlag(1);
                    dish.setUpdateTime(LocalDateTime.now());
                    dishMapper.updateById(dish);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 软删除菜谱及其相关的关联关系
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDish(Long dishId) {
        DietDish dish = dishMapper.selectById(dishId);
        if (dish != null) {
            dish.setDelFlag(1); // 软删除
            dish.setUpdateTime(LocalDateTime.now());
            int updated = dishMapper.updateById(dish);
            if (updated > 0) {
                // 1. 软删除其所有做法分支
                LambdaQueryWrapper<DietDishCookingBranch> branchQuery = new LambdaQueryWrapper<>();
                branchQuery.eq(DietDishCookingBranch::getDishId, dishId);
                List<DietDishCookingBranch> branches = dishCookingBranchMapper.selectList(branchQuery);
                for (DietDishCookingBranch b : branches) {
                    b.setDelFlag(1);
                    b.setUpdateTime(LocalDateTime.now());
                    dishCookingBranchMapper.updateById(b);
                }

                // 2. 同步软删除其食材与步骤配方关联
                LambdaQueryWrapper<DietDishIngredient> ingQuery = new LambdaQueryWrapper<>();
                ingQuery.eq(DietDishIngredient::getDishId, dishId);
                List<DietDishIngredient> ingredients = dishIngredientMapper.selectList(ingQuery);
                for (DietDishIngredient ing : ingredients) {
                    ing.setDelFlag(1);
                    ing.setUpdateTime(LocalDateTime.now());
                    dishIngredientMapper.updateById(ing);
                }

                LambdaQueryWrapper<DietDishStepRelation> stepQuery = new LambdaQueryWrapper<>();
                stepQuery.eq(DietDishStepRelation::getDishId, dishId);
                List<DietDishStepRelation> steps = dishStepRelationMapper.selectList(stepQuery);
                for (DietDishStepRelation step : steps) {
                    step.setDelFlag(1);
                    step.setUpdateTime(LocalDateTime.now());
                    dishStepRelationMapper.updateById(step);
                }
                return true;
            }
        }
        return false;
    }
}
