package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietDishIngredientMapper;
import com.diet.modules.biz.mapper.DietDishMapper;
import com.diet.modules.biz.mapper.DietDishStepRelationMapper;
import com.diet.modules.biz.model.dto.*;
import com.diet.modules.biz.model.entity.*;
import com.diet.modules.biz.model.po.DietDishQueryPO;
import com.diet.modules.biz.model.vo.*;
import com.diet.modules.biz.util.NutrientCalcUtil;
import com.diet.modules.common.aspect.DictData;
import com.diet.modules.common.entity.BaseDeleteDTO;
import com.diet.modules.system.model.entity.SysFileStorage;
import com.diet.modules.system.service.SysDataDictionaryService;
import com.diet.modules.system.service.SysFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    // 引入其他表的 Service，而非 Mapper
    private final DietDishCookingBranchService dishCookingBranchService;
    private final DietDishIngredientService dishIngredientService;
    private final DietIngredientService ingredientService;
    private final DietDishStepRelationService dishStepRelationService;
    private final DietCookingStepPoolService cookingStepPoolService;
    private final DietCookDishStatService cookDishStatService;
    private final DietCookSkilledDishService cookSkilledDishService;
    private final DietUserWishDishService userWishDishService;
    private final DietUserDislikeDishService userDislikeDishService;
    private final DietUserDishImageService userDishImageService;
    private final SysFileStorageService sysFileStorageService;
    private final SysDataDictionaryService sysDataDictionaryService;

    // 保留需要跨表联查的 Mapper，因为多表联查要求使用 Mapper XML SQL 模式
    private final DietDishIngredientMapper dishIngredientMapper;
    private final DietDishStepRelationMapper dishStepRelationMapper;

    /**
     * 获取所有有效的菜谱及对应的熟练度与图片
     */
    public List<DietDishVO> listDishes(DietDishQueryPO po) {
        List<DietDish> dishes = this.lambdaQuery()
                .eq(DietDish::getDelFlag, 0)
                .list();

        Map<Long, DietCookDishStat> statMap = getCookDishStatMap(po.getUserId());
        Set<Long> skilledDishIds = getSkilledDishIds(po.getUserId());
        Map<Long, String> customImageMap = getCustomImageMap(po.getGroupId());

        List<DietDishVO> response = new ArrayList<>();
        for (DietDish dish : dishes) {
            DietDishVO vo = new DietDishVO();
            BeanUtil.copyProperties(dish, vo);

            // 填充预览图片
            vo.setPreviewUrl(getPreviewUrl(dish, customImageMap));

            // 填充统计指标
            DietCookDishStat stat = statMap.get(dish.getDishId());
            vo.setCookCount(stat != null ? stat.getCookCount() : 0);
            vo.setSignatureFlag(stat != null ? stat.getSignatureFlag() : 0);
            vo.setIsSkilled(skilledDishIds.contains(dish.getDishId()) ? 1 : 0);

            response.add(vo);
        }

        return response;
    }

    private Map<Long, DietCookDishStat> getCookDishStatMap(Long userId) {
        Map<Long, DietCookDishStat> statMap = new HashMap<>();
        if (userId == null) {
            return statMap;
        }
        List<DietCookDishStat> stats = cookDishStatService.lambdaQuery()
                .eq(DietCookDishStat::getUserId, userId).eq(DietCookDishStat::getDelFlag, 0).list();
        for (DietCookDishStat stat : stats) {
            statMap.put(stat.getDishId(), stat);
        }
        return statMap;
    }

    private Set<Long> getSkilledDishIds(Long userId) {
        if (userId == null) {
            return new HashSet<>();
        }
        List<DietCookSkilledDish> skilled = cookSkilledDishService.lambdaQuery()
                .eq(DietCookSkilledDish::getUserId, userId).eq(DietCookSkilledDish::getDelFlag, 0).list();
        return skilled.stream().map(DietCookSkilledDish::getDishId).collect(Collectors.toSet());
    }

    private Map<Long, String> getCustomImageMap(Long groupId) {
        Map<Long, String> customImageMap = new HashMap<>();
        if (groupId == null) {
            return customImageMap;
        }
        List<DietUserDishImage> userImages = userDishImageService.lambdaQuery()
                .eq(DietUserDishImage::getGroupId, groupId).eq(DietUserDishImage::getDelFlag, 0).list();
        for (DietUserDishImage userImg : userImages) {
            SysFileStorage fs = sysFileStorageService.getById(userImg.getStorageId());
            if (fs != null && fs.getDelFlag() == 0) {
                customImageMap.put(userImg.getDishId(), fs.getFilePath());
            }
        }
        return customImageMap;
    }

    private String getPreviewUrl(DietDish dish, Map<Long, String> customImageMap) {
        String previewUrl = customImageMap.get(dish.getDishId());
        if (previewUrl == null && dish.getCoverImageId() != null) {
            SysFileStorage defaultImg = sysFileStorageService.getById(dish.getCoverImageId());
            if (defaultImg != null && defaultImg.getDelFlag() == 0) {
                previewUrl = defaultImg.getFilePath();
            }
        }
        return previewUrl;
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
                fillBranchCompatList(vo);
            }
        }
        return result;
    }

    private void fillBranchCompatList(DietDishVO vo) {
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

    /**
     * 获取指定菜谱的详情信息 (配料原料列表和做法步骤列表)
     * 多表关联使用 Mapper XML SQL 连表查询模式
     */
    public DietDishDetailVO getDishDetail(Long dishId) {
        DietDish dish = dishMapper.selectById(dishId);
        if (dish == null || dish.getDelFlag() == 1) {
            throw new RuntimeException("菜品不存在");
        }

        DietDishDetailVO detailVO = new DietDishDetailVO();
        detailVO.setDish(dish);

        List<DietDishCookingBranch> branches = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDishId, dishId).eq(DietDishCookingBranch::getDelFlag, 0).list();

        if (branches.isEmpty()) {
            branches = createDefaultBranchCompat(dishId);
        }

        List<DietDishBranchVO> branchVOList = new ArrayList<>();
        for (DietDishCookingBranch branch : branches) {
            DietDishBranchVO branchVO = new DietDishBranchVO();
            BeanUtil.copyProperties(branch, branchVO);

            // 填充做法分支图片
            branchVO.setPreviewUrl(getBranchPreviewUrl(branch));

            // 1. 获取配方原料列表 (多表关联查询)
            List<DietDishIngredientVO> ingredients = dishIngredientMapper.selectRecipeIngredientsByBranchId(branch.getBranchId());
            branchVO.setIngredients(ingredients);

            // 2. 获取做法步骤列表 (多表关联查询)
            List<DietDishStepVO> steps = dishStepRelationMapper.selectRecipeStepsByBranchId(branch.getBranchId());
            if (CollUtil.isNotEmpty(steps)) {
                for (DietDishStepVO step : steps) {
                    if (step.getFirePower() != null) {
                        step.setFirePowerLabel(sysDataDictionaryService.selectValueByTypeAndCode("fire_power_type", step.getFirePower().toString()));
                    }
                }
            }
            branchVO.setSteps(steps);

            branchVOList.add(branchVO);
        }
        detailVO.setBranches(branchVOList);

        if (!branchVOList.isEmpty()) {
            detailVO.setIngredients(branchVOList.get(0).getIngredients());
            detailVO.setSteps(branchVOList.get(0).getSteps());
        }

        return detailVO;
    }

    private String getBranchPreviewUrl(DietDishCookingBranch branch) {
        if (branch.getCoverImageId() != null) {
            SysFileStorage img = sysFileStorageService.getById(branch.getCoverImageId());
            if (img != null && img.getDelFlag() == 0) {
                return img.getFilePath();
            }
        }
        return null;
    }

    private List<DietDishCookingBranch> createDefaultBranchCompat(Long dishId) {
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
        dishCookingBranchService.save(defaultBranch);

        Long defaultBranchId = defaultBranch.getBranchId();

        // 级联修改配料关系
        DietDishIngredient updateIng = new DietDishIngredient();
        updateIng.setBranchId(defaultBranchId);
        dishIngredientService.lambdaUpdate()
                .eq(DietDishIngredient::getDishId, dishId)
                .update(updateIng);

        // 级联修改步骤关系
        DietDishStepRelation updateStep = new DietDishStepRelation();
        updateStep.setBranchId(defaultBranchId);
        dishStepRelationService.lambdaUpdate()
                .eq(DietDishStepRelation::getDishId, dishId)
                .update(updateStep);

        return Collections.singletonList(defaultBranch);
    }

    /**
     * 标记普通家庭成员对特定菜谱表示的忌口或讨厌反馈
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean markDislike(DietDislikeDTO dto) {
        if (dto == null || dto.getProfileId() == null || dto.getGroupId() == null || dto.getDishId() == null) {
            return false;
        }

        DietUserDislikeDish existing = userDislikeDishService.lambdaQuery()
                .eq(DietUserDislikeDish::getProfileId, dto.getProfileId())
                .eq(DietUserDislikeDish::getDishId, dto.getDishId())
                .one();

        if (existing != null) {
            existing.setDislikeCount(existing.getDislikeCount() + 1);
            existing.setUpdateTime(LocalDateTime.now());
            return userDislikeDishService.updateById(existing);
        } else {
            DietUserDislikeDish d = new DietUserDislikeDish();
            d.setProfileId(dto.getProfileId());
            d.setGroupId(dto.getGroupId());
            d.setDishId(dto.getDishId());
            d.setDislikeCount(1);
            d.setDelFlag(0);
            return userDislikeDishService.save(d);
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
        return userWishDishService.save(w);
    }

    /**
     * 添加或取消拿手菜/擅长标记
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean manageSkilled(DietSkilledDTO dto) {
        if (dto == null || dto.getUserId() == null || dto.getDishId() == null || dto.getIsSkilled() == null) {
            return false;
        }

        DietCookSkilledDish existing = cookSkilledDishService.lambdaQuery()
                .eq(DietCookSkilledDish::getUserId, dto.getUserId())
                .eq(DietCookSkilledDish::getDishId, dto.getDishId())
                .one();

        if (dto.getIsSkilled() == 1) { // 添加擅长
            if (existing == null) {
                DietCookSkilledDish skill = new DietCookSkilledDish();
                skill.setUserId(dto.getUserId());
                skill.setDishId(dto.getDishId());
                skill.setDelFlag(0);
                return cookSkilledDishService.save(skill);
            } else if (existing.getDelFlag() == 1) {
                existing.setDelFlag(0);
                existing.setUpdateTime(LocalDateTime.now());
                return cookSkilledDishService.updateById(existing);
            }
            return true;
        } else { // 移除擅长
            if (existing != null && existing.getDelFlag() == 0) {
                existing.setDelFlag(1); // 软删除
                existing.setUpdateTime(LocalDateTime.now());
                return cookSkilledDishService.updateById(existing);
            }
            return true;
        }
    }

    /**
     * 保存或更新菜谱，包含其配料与步骤关联
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveDish(DietDishSaveDTO dto) {
        if (dto == null || dto.getDishName() == null || dto.getDishName().trim().isEmpty()) {
            return false;
        }

        // 1. 保存/更新菜品主表
        DietDish dish = saveOrUpdateDishMain(dto);
        Long dishId = dish.getDishId();

        // 2. 清理废弃的做法分支
        List<DietDishBranchSaveDTO> branches = dto.getBranches();
        if (branches == null || branches.isEmpty()) {
            branches = createDefaultBranchDTOList(dto);
        }
        deleteObsoleteBranches(dishId, branches);

        // 3. 处理做法分支及其关联
        saveOrUpdateBranches(dishId, branches);

        return true;
    }

    private DietDish saveOrUpdateDishMain(DietDishSaveDTO dto) {
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

        if (dto.getDishId() != null) {
            dishMapper.updateById(dish);
        } else {
            dishMapper.insert(dish);
        }
        return dish;
    }

    private List<DietDishBranchSaveDTO> createDefaultBranchDTOList(DietDishSaveDTO dto) {
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
        return Collections.singletonList(defaultBranch);
    }

    private void deleteObsoleteBranches(Long dishId, List<DietDishBranchSaveDTO> branches) {
        Set<Long> incomingBranchIds = branches.stream()
                .map(DietDishBranchSaveDTO::getBranchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<DietDishCookingBranch> toDeleteBranches = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDishId, dishId)
                .notIn(!incomingBranchIds.isEmpty(), DietDishCookingBranch::getBranchId, incomingBranchIds)
                .list();
        for (DietDishCookingBranch b : toDeleteBranches) {
            b.setDelFlag(1);
            b.setUpdateTime(LocalDateTime.now());
            dishCookingBranchService.updateById(b);

            dishIngredientService.lambdaUpdate()
                    .eq(DietDishIngredient::getBranchId, b.getBranchId())
                    .remove();

            dishStepRelationService.lambdaUpdate()
                    .eq(DietDishStepRelation::getBranchId, b.getBranchId())
                    .remove();
        }
    }

    private void saveOrUpdateBranches(Long dishId, List<DietDishBranchSaveDTO> branches) {
        for (DietDishBranchSaveDTO branchDTO : branches) {
            DietDishCookingBranch branch = new DietDishCookingBranch();
            if (branchDTO.getBranchId() != null) {
                branch = dishCookingBranchService.getById(branchDTO.getBranchId());
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

            // 智能核算热量与三大营养素 (利用 NutrientCalcUtil)
            calculateAndSetNutrients(branch, branchDTO);

            if (branch.getBranchId() != null) {
                dishCookingBranchService.updateById(branch);
            } else {
                dishCookingBranchService.save(branch);
            }
            Long branchId = branch.getBranchId();

            // 重建配料及步骤关联
            rebuildBranchRelations(dishId, branchId, branchDTO);
        }
    }

    private void calculateAndSetNutrients(DietDishCookingBranch branch, DietDishBranchSaveDTO branchDTO) {
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
                List<NutrientCalcUtil.IngredientNutrientInfo> calcList = new ArrayList<>();
                for (DietDishIngredientSaveDTO ingDto : branchDTO.getIngredients()) {
                    DietIngredient ing = ingredientService.getById(ingDto.getIngredientId());
                    if (ing != null && ing.getDelFlag() == 0 && ingDto.getUseAmount() != null) {
                        calcList.add(new NutrientCalcUtil.IngredientNutrientInfo(
                                ingDto.getUseAmount(),
                                ing.getCalories(),
                                ing.getProtein(),
                                ing.getFat(),
                                ing.getCarbs()
                        ));
                    }
                }
                NutrientCalcUtil.CalculatedNutrients calc = NutrientCalcUtil.calculate(calcList);
                branch.setCalories(calc.getCalories());
                branch.setProtein(calc.getProtein());
                branch.setFat(calc.getFat());
                branch.setCarbs(calc.getCarbs());
            }
        }
    }

    private void rebuildBranchRelations(Long dishId, Long branchId, DietDishBranchSaveDTO branchDTO) {
        // 清理此做法分支已有的配料与步骤关联
        dishIngredientService.lambdaUpdate()
                .eq(DietDishIngredient::getBranchId, branchId)
                .remove();

        dishStepRelationService.lambdaUpdate()
                .eq(DietDishStepRelation::getBranchId, branchId)
                .remove();

        // 插入配料关系
        if (branchDTO.getIngredients() != null) {
            List<DietDishIngredient> rels = branchDTO.getIngredients().stream().map(ingDto -> {
                DietDishIngredient rel = new DietDishIngredient();
                rel.setDishId(dishId);
                rel.setBranchId(branchId);
                rel.setIngredientId(ingDto.getIngredientId());
                rel.setUseAmount(ingDto.getUseAmount());
                rel.setMainMaterialFlag(ingDto.getMainMaterialFlag() != null ? ingDto.getMainMaterialFlag() : 1);
                rel.setDelFlag(0);
                rel.setCreateTime(LocalDateTime.now());
                rel.setUpdateTime(LocalDateTime.now());
                return rel;
            }).collect(Collectors.toList());
            dishIngredientService.saveBatch(rels);
        }

        // 插入步骤关系
        if (branchDTO.getSteps() != null) {
            List<DietDishStepRelation> stepRels = branchDTO.getSteps().stream().map(stepDto -> {
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
                return rel;
            }).collect(Collectors.toList());
            dishStepRelationService.saveBatch(stepRels);
        }
    }

    /**
     * 批量或单条软删除菜品
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDishes(BaseDeleteDTO dto) {
        if (dto == null) {
            return false;
        }
        Set<Long> ids = dto.allIds();
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        // 软删除主菜谱记录
        List<DietDish> list = ids.stream().map(id -> {
            DietDish entity = new DietDish();
            entity.setDishId(id);
            entity.setDelFlag(1); // 软删除
            entity.setUpdateTime(LocalDateTime.now());
            return entity;
        }).collect(Collectors.toList());

        boolean updated = updateBatchById(list);
        if (updated) {
            for (Long id : ids) {
                cascadeDeleteBranchByDishId(id);
            }
            return true;
        }
        return false;
    }

    private void cascadeDeleteBranchByDishId(Long dishId) {
        // 1. 软删除其所有做法分支
        List<DietDishCookingBranch> branches = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDishId, dishId)
                .list();
        for (DietDishCookingBranch b : branches) {
            b.setDelFlag(1);
            b.setUpdateTime(LocalDateTime.now());
            dishCookingBranchService.updateById(b);
        }

        // 2. 同步软删除其食材与步骤配方关联
        List<DietDishIngredient> ingredients = dishIngredientService.lambdaQuery()
                .eq(DietDishIngredient::getDishId, dishId)
                .list();
        for (DietDishIngredient ing : ingredients) {
            ing.setDelFlag(1);
            ing.setUpdateTime(LocalDateTime.now());
            dishIngredientService.updateById(ing);
        }

        List<DietDishStepRelation> steps = dishStepRelationService.lambdaQuery()
                .eq(DietDishStepRelation::getDishId, dishId)
                .list();
        for (DietDishStepRelation step : steps) {
            step.setDelFlag(1);
            step.setUpdateTime(LocalDateTime.now());
            dishStepRelationService.updateById(step);
        }
    }

    /**
     * 批量或单条软删除特定做法分支。如果该菜谱已无其他有效分支，则同步软删除菜谱主表。
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBranches(BaseDeleteDTO dto) {
        if (dto == null) {
            return false;
        }
        Set<Long> ids = dto.allIds();
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        boolean anySuccess = false;
        for (Long branchId : ids) {
            DietDishCookingBranch branch = dishCookingBranchService.getById(branchId);
            if (branch != null && branch.getDelFlag() == 0) {
                // 1. 软删除此做法分支
                branch.setDelFlag(1);
                branch.setUpdateTime(LocalDateTime.now());
                int updated = dishCookingBranchService.updateById(branch) ? 1 : 0;
                if (updated > 0) {
                    // 2. 清除配料及步骤关联
                    dishIngredientService.lambdaUpdate()
                            .eq(DietDishIngredient::getBranchId, branchId)
                            .remove();

                    dishStepRelationService.lambdaUpdate()
                            .eq(DietDishStepRelation::getBranchId, branchId)
                            .remove();

                    // 3. 检查级联删除主菜谱记录
                    checkAndCascadeDeleteDish(branch.getDishId());
                    anySuccess = true;
                }
            }
        }
        return anySuccess;
    }

    private void checkAndCascadeDeleteDish(Long dishId) {
        long activeCount = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDishId, dishId)
                .eq(DietDishCookingBranch::getDelFlag, 0)
                .count();
        if (activeCount == 0) {
            DietDish dish = dishMapper.selectById(dishId);
            if (dish != null && dish.getDelFlag() == 0) {
                dish.setDelFlag(1);
                dish.setUpdateTime(LocalDateTime.now());
                dishMapper.updateById(dish);
            }
        }
    }
}
