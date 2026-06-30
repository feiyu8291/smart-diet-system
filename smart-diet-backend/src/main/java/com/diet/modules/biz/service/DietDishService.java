package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
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

    // 引入其他表的 Service，而非 Mapper
    private final DietDishCookingBranchService dishCookingBranchService;
    private final DietDishIngredientService dishIngredientService;
    private final DietIngredientService ingredientService;
    private final DietDishStepRelationService dishStepRelationService;
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
     *
     * @param po 菜谱查询入参 PO
     * @return 菜谱VO列表
     */
    public List<DietDishVO> listDishes(DietDishQueryPO po) {
        List<DietDish> dishes = this.lambdaQuery()
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
            vo.setCookCount(Objects.nonNull(stat) ? stat.getCookCount() : 0);
            vo.setSignatureFlag(Objects.nonNull(stat) ? stat.getSignatureFlag() : 0);
            vo.setIsSkilled(skilledDishIds.contains(dish.getDishId()) ? 1 : 0);

            response.add(vo);
        }

        return response;
    }

    /**
     * 获取做饭人的菜品统计Map
     *
     * @param userId 做饭人用户ID
     * @return 菜品ID与统计信息的映射Map
     */
    private Map<Long, DietCookDishStat> getCookDishStatMap(Long userId) {
        Map<Long, DietCookDishStat> statMap = new HashMap<>();
        if (Objects.isNull(userId)) {
            return statMap;
        }
        List<DietCookDishStat> stats = cookDishStatService.lambdaQuery()
                .eq(DietCookDishStat::getUserId, userId)
                .list();
        for (DietCookDishStat stat : stats) {
            statMap.put(stat.getDishId(), stat);
        }
        return statMap;
    }

    /**
     * 获取拿手菜品ID集合
     *
     * @param userId 做饭人用户ID
     * @return 拿手菜品ID的Set集合
     */
    private Set<Long> getSkilledDishIds(Long userId) {
        if (Objects.isNull(userId)) {
            return new HashSet<>();
        }
        List<DietCookSkilledDish> skilled = cookSkilledDishService.lambdaQuery()
                .eq(DietCookSkilledDish::getUserId, userId)
                .list();
        return skilled.stream().map(DietCookSkilledDish::getDishId).collect(Collectors.toSet());
    }

    /**
     * 获取家庭组自定义上传封面图片的映射Map
     *
     * @param groupId 家庭组ID
     * @return 菜品ID与封面图物理路径的映射Map
     */
    private Map<Long, String> getCustomImageMap(Long groupId) {
        Map<Long, String> customImageMap = new HashMap<>();
        if (Objects.isNull(groupId)) {
            return customImageMap;
        }
        List<DietUserDishImage> userImages = userDishImageService.lambdaQuery()
                .eq(DietUserDishImage::getGroupId, groupId)
                .list();
        for (DietUserDishImage userImg : userImages) {
            SysFileStorage fs = sysFileStorageService.getById(userImg.getStorageId());
            if (Objects.nonNull(fs)) {
                customImageMap.put(userImg.getDishId(), fs.getFilePath());
            }
        }
        return customImageMap;
    }

    /**
     * 根据自定义与全局默认封面图片，获取预览图URL
     *
     * @param dish           菜品实体
     * @param customImageMap 组封面图映射Map
     * @return 预览路径
     */
    private String getPreviewUrl(DietDish dish, Map<Long, String> customImageMap) {
        String previewUrl = customImageMap.get(dish.getDishId());
        if (Objects.isNull(previewUrl) && Objects.nonNull(dish.getCoverImageId())) {
            SysFileStorage defaultImg = sysFileStorageService.getById(dish.getCoverImageId());
            if (Objects.nonNull(defaultImg)) {
                previewUrl = defaultImg.getFilePath();
            }
        }
        return previewUrl;
    }

    /**
     * 分页查询菜谱列表及做饭人统计信息
     *
     * @param po 菜谱查询入参 PO
     * @return 菜谱VO的分页包装实体
     */
    @DictData
    public IPage<DietDishVO> pageDishes(DietDishQueryPO po) {
        Page<DietDishVO> page = new Page<>(po.getPageNo(), po.getPageSize());
        IPage<DietDishVO> result = this.baseMapper.selectDishPage(page, po);
        List<DietDishVO> records = result.getRecords();
        if (Objects.nonNull(records) && !records.isEmpty()) {
            for (DietDishVO vo : records) {
                fillBranchCompatList(vo);
            }
        }
        return result;
    }

    /**
     * 为单表菜谱VO填充单做法分支的兼容数据列表
     *
     * @param vo 菜谱VO实体
     */
    private void fillBranchCompatList(DietDishVO vo) {
        List<DietDishBranchVO> branchVOList = new ArrayList<>();
        DietDishBranchVO branchVO = new DietDishBranchVO();
        branchVO.setBranchId(vo.getBranchId());
        branchVO.setBranchName(vo.getBranchName());
        branchVO.setCuisineType(vo.getCuisineType());
        branchVO.setDietMode(Objects.nonNull(vo.getDietMode()) ? Integer.parseInt(vo.getDietMode()) : 0);
        branchVO.setCalories(Objects.nonNull(vo.getCalories()) ? BigDecimal.valueOf(vo.getCalories()) : BigDecimal.ZERO);
        branchVO.setProtein(vo.getProtein());
        branchVO.setFat(vo.getFat());
        branchVO.setCarbs(vo.getCarbs());
        branchVOList.add(branchVO);
        vo.setBranches(branchVOList);
    }

    /**
     * 获取指定菜谱的详情信息 (配料原料列表和做法步骤列表)
     * 多表关联使用 Mapper XML SQL 连表查询模式
     *
     * @param dishId 菜谱主表ID
     * @return 菜谱详情VO，包含其分支、原料和烹饪步骤列表
     */
    public DietDishDetailVO getDishDetail(Long dishId) {
        DietDish dish = this.baseMapper.selectById(dishId);
        if (Objects.isNull(dish)) {
            throw new RuntimeException("菜品不存在");
        }

        DietDishDetailVO detailVO = new DietDishDetailVO();
        detailVO.setDish(dish);

        List<DietDishCookingBranch> branches = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDishId, dishId)
                .list();

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
                    if (Objects.nonNull(step.getFirePower())) {
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

    /**
     * 获取做法分支的封面图片物理路径
     *
     * @param branch 做法分支实体
     * @return 封面图的物理存储路径，若无则返回null
     */
    private String getBranchPreviewUrl(DietDishCookingBranch branch) {
        if (Objects.nonNull(branch.getCoverImageId())) {
            SysFileStorage img = sysFileStorageService.getById(branch.getCoverImageId());
            if (Objects.nonNull(img)) {
                return img.getFilePath();
            }
        }
        return null;
    }

    /**
     * 创建默认的做法分支兼容数据 (主要用于处理历史老数据无分支的情况)
     *
     * @param dishId 菜品ID
     * @return 默认创建的做法分支列表
     */
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
     *
     * @param dto 忌口反馈入参 DTO
     * @return 标记是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean markDislike(DietDislikeDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getProfileId()) || Objects.isNull(dto.getGroupId()) || Objects.isNull(dto.getDishId())) {
            return false;
        }

        DietUserDislikeDish existing = userDislikeDishService.lambdaQuery()
                .eq(DietUserDislikeDish::getProfileId, dto.getProfileId())
                .eq(DietUserDislikeDish::getDishId, dto.getDishId())
                .one();

        if (Objects.nonNull(existing)) {
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
     *
     * @param dto 意向菜品入参 DTO
     * @return 添加是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean addWish(DietWishDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getProfileId()) || Objects.isNull(dto.getGroupId()) || Objects.isNull(dto.getDishId())) {
            return false;
        }

        DietUserWishDish w = new DietUserWishDish();
        w.setProfileId(dto.getProfileId());
        w.setGroupId(dto.getGroupId());
        w.setDishId(dto.getDishId());
        if (CharSequenceUtil.isNotBlank(dto.getWishDate())) {
            w.setWishDate(LocalDate.parse(dto.getWishDate()));
        }
        return userWishDishService.save(w);
    }

    /**
     * 添加或取消拿手菜/擅长标记
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean manageSkilled(DietSkilledDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getUserId()) || Objects.isNull(dto.getDishId()) || Objects.isNull(dto.getIsSkilled())) {
            return false;
        }

        DietCookSkilledDish existing = cookSkilledDishService.lambdaQuery()
                .eq(DietCookSkilledDish::getUserId, dto.getUserId())
                .eq(DietCookSkilledDish::getDishId, dto.getDishId())
                .one();

        if (dto.getIsSkilled() == 1) { // 添加擅长
            if (Objects.isNull(existing)) {
                DietCookSkilledDish skill = new DietCookSkilledDish();
                skill.setUserId(dto.getUserId());
                skill.setDishId(dto.getDishId());
                return cookSkilledDishService.save(skill);
            }
        } else { // 移除擅长
            if (Objects.nonNull(existing)) {
                return cookSkilledDishService.removeById(existing);
            }
        }
        return true;
    }

    /**
     * 保存或更新菜谱，包含其配料与步骤关联
     *
     * @param dto 菜谱保存参数 DTO
     * @return 保存是否成功
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
        if (Objects.isNull(branches) || branches.isEmpty()) {
            branches = createDefaultBranchDTOList(dto);
        }
        deleteObsoleteBranches(dishId, branches);

        // 3. 处理做法分支及其关联
        saveOrUpdateBranches(dishId, branches);

        return true;
    }

    /**
     * 保存或更新菜品主表数据
     *
     * @param dto 菜谱保存参数 DTO
     * @return 保存后的菜品实体
     */
    private DietDish saveOrUpdateDishMain(DietDishSaveDTO dto) {
        DietDish dish = new DietDish();
        if (Objects.nonNull(dto.getDishId())) {
            dish = this.baseMapper.selectById(dto.getDishId());
            if (Objects.isNull(dish)) {
                throw new RuntimeException("修改的菜品不存在！");
            }
        } else {
            dish.setDelFlag(0);
            dish.setCreateTime(LocalDateTime.now());
        }

        dish.setDishName(dto.getDishName());
        dish.setCoverImageId(dto.getCoverImageId());
        dish.setUpdateTime(LocalDateTime.now());

        if (Objects.nonNull(dto.getDishId())) {
            this.baseMapper.updateById(dish);
        } else {
            this.baseMapper.insert(dish);
        }
        return dish;
    }

    /**
     * 构建无分支菜谱时默认的单分支包装DTO列表
     *
     * @param dto 菜谱保存参数 DTO
     * @return 默认经典做法分支DTO列表
     */
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

    /**
     * 清理（批量删除）数据库中已被前台移出的做法分支及其关联配料、步骤
     *
     * @param dishId   菜谱主表ID
     * @param branches 当前传入做法分支DTO列表
     */
    private void deleteObsoleteBranches(Long dishId, List<DietDishBranchSaveDTO> branches) {
        Set<Long> incomingBranchIds = branches.stream()
                .map(DietDishBranchSaveDTO::getBranchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<DietDishCookingBranch> toDeleteBranches = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDishId, dishId)
                .notIn(CollUtil.isNotEmpty(incomingBranchIds), DietDishCookingBranch::getBranchId, incomingBranchIds)
                .list();

        if (CollUtil.isNotEmpty(toDeleteBranches)) {
            List<Long> toDeleteBranchIds = toDeleteBranches.stream().map(DietDishCookingBranch::getBranchId).toList();
            dishCookingBranchService.removeBatchByIds(toDeleteBranchIds);

            dishIngredientService.lambdaUpdate()
                    .in(DietDishIngredient::getBranchId, toDeleteBranchIds)
                    .remove();

            dishStepRelationService.lambdaUpdate()
                    .in(DietDishStepRelation::getBranchId, toDeleteBranchIds)
                    .remove();
        }
    }

    /**
     * 保存或更新多个做法分支的数据
     *
     * @param dishId   菜谱主表ID
     * @param branches 做法分支DTO列表
     */
    private void saveOrUpdateBranches(Long dishId, List<DietDishBranchSaveDTO> branches) {
        for (DietDishBranchSaveDTO branchDTO : branches) {
            DietDishCookingBranch branch = new DietDishCookingBranch();
            if (Objects.nonNull(branchDTO.getBranchId())) {
                branch = dishCookingBranchService.getById(branchDTO.getBranchId());
                if (Objects.isNull(branch)) {
                    throw new RuntimeException("修改的做法分支不存在！");
                }
            } else {
                branch.setDishId(dishId);
                branch.setDelFlag(0);
                branch.setCreateTime(LocalDateTime.now());
            }

            branch.setBranchName(branchDTO.getBranchName());
            branch.setCuisineType(Objects.nonNull(branchDTO.getCuisineType()) ? branchDTO.getCuisineType() : "粤菜");
            branch.setDietMode(Objects.nonNull(branchDTO.getDietMode()) ? branchDTO.getDietMode() : 0);
            branch.setCoverImageId(branchDTO.getCoverImageId());
            branch.setUpdateTime(LocalDateTime.now());

            // 智能核算热量与三大营养素 (利用 NutrientCalcUtil)
            calculateAndSetNutrients(branch, branchDTO);

            if (Objects.nonNull(branch.getBranchId())) {
                dishCookingBranchService.updateById(branch);
            } else {
                dishCookingBranchService.save(branch);
            }
            Long branchId = branch.getBranchId();

            // 重建配料及步骤关联
            rebuildBranchRelations(dishId, branchId, branchDTO);
        }
    }

    /**
     * 智能核算做法分支的热量及三大核心营养素 (支持自动计算与手填)
     *
     * @param branch    做法分支实体
     * @param branchDTO 做法分支DTO
     */
    private void calculateAndSetNutrients(DietDishCookingBranch branch, DietDishBranchSaveDTO branchDTO) {
        boolean needsCalc = (Objects.nonNull(branchDTO.getAutoCalculateNutrients()) && branchDTO.getAutoCalculateNutrients())
                || (Objects.isNull(branchDTO.getCalories()) || branchDTO.getCalories().doubleValue() <= 0);

        if (!needsCalc) {
            branch.setCalories(branchDTO.getCalories());
            branch.setProtein(Objects.nonNull(branchDTO.getProtein()) ? branchDTO.getProtein() : BigDecimal.ZERO);
            branch.setFat(Objects.nonNull(branchDTO.getFat()) ? branchDTO.getFat() : BigDecimal.ZERO);
            branch.setCarbs(Objects.nonNull(branchDTO.getCarbs()) ? branchDTO.getCarbs() : BigDecimal.ZERO);
        } else {
            if (Objects.isNull(branchDTO.getIngredients()) || branchDTO.getIngredients().isEmpty()) {
                branch.setCalories(BigDecimal.ZERO);
                branch.setProtein(BigDecimal.ZERO);
                branch.setFat(BigDecimal.ZERO);
                branch.setCarbs(BigDecimal.ZERO);
            } else {
                List<NutrientCalcUtil.IngredientNutrientInfo> calcList = new ArrayList<>();
                for (DietDishIngredientSaveDTO ingDto : branchDTO.getIngredients()) {
                    DietIngredient ing = ingredientService.getById(ingDto.getIngredientId());
                    if (Objects.nonNull(ing) && Objects.nonNull(ingDto.getUseAmount())) {
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

    /**
     * 清理并重建此做法分支与配料、烹饪步骤的多表对多表关联关系
     *
     * @param dishId    菜品ID
     * @param branchId  做法分支ID
     * @param branchDTO 做法分支数据 DTO
     */
    private void rebuildBranchRelations(Long dishId, Long branchId, DietDishBranchSaveDTO branchDTO) {
        // 清理此做法分支已有的配料与步骤关联
        dishIngredientService.lambdaUpdate()
                .eq(DietDishIngredient::getBranchId, branchId)
                .remove();

        dishStepRelationService.lambdaUpdate()
                .eq(DietDishStepRelation::getBranchId, branchId)
                .remove();

        // 插入配料关系
        if (Objects.nonNull(branchDTO.getIngredients())) {
            List<DietDishIngredient> rels = branchDTO.getIngredients().stream().map(ingDto -> {
                DietDishIngredient rel = new DietDishIngredient();
                rel.setDishId(dishId);
                rel.setBranchId(branchId);
                rel.setIngredientId(ingDto.getIngredientId());
                rel.setUseAmount(ingDto.getUseAmount());
                rel.setMainMaterialFlag(Objects.nonNull(ingDto.getMainMaterialFlag()) ? ingDto.getMainMaterialFlag() : 1);
                rel.setDelFlag(0);
                rel.setCreateTime(LocalDateTime.now());
                rel.setUpdateTime(LocalDateTime.now());
                return rel;
            }).collect(Collectors.toList());
            dishIngredientService.saveBatch(rels);
        }

        // 插入步骤关系
        if (Objects.nonNull(branchDTO.getSteps())) {
            List<DietDishStepRelation> stepRels = branchDTO.getSteps().stream().map(stepDto -> {
                DietDishStepRelation rel = new DietDishStepRelation();
                rel.setDishId(dishId);
                rel.setBranchId(branchId);
                rel.setStepPoolId(Objects.nonNull(stepDto.getStepPoolId()) ? stepDto.getStepPoolId() : 0L);
                rel.setStepNum(stepDto.getStepNum());
                rel.setCustomDetail(stepDto.getCustomDetail());
                rel.setDurationSeconds(Objects.nonNull(stepDto.getDurationSeconds()) ? stepDto.getDurationSeconds() : 0);
                rel.setFirePower(Objects.nonNull(stepDto.getFirePower()) ? stepDto.getFirePower() : 0);
                rel.setDelFlag(0);
                rel.setCreateTime(LocalDateTime.now());
                rel.setUpdateTime(LocalDateTime.now());
                return rel;
            }).collect(Collectors.toList());
            dishStepRelationService.saveBatch(stepRels);
        }
    }

    /**
     * 批量或单条软删除菜品，并级联下属分支及关联
     *
     * @param dto 批量删除入参 DTO
     * @return 删除是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDishes(BaseDeleteDTO dto) {
        if (Objects.isNull(dto)) {
            return false;
        }
        Set<Long> ids = dto.allIds();
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        // 批量软删除主菜谱记录
        boolean updated = this.removeBatchByIds(ids);
        if (updated) {
            for (Long id : ids) {
                cascadeDeleteBranchByDishId(id);
            }
            return true;
        }
        return false;
    }

    /**
     * 级联删除指定菜品关联的所有做法分支及食材、步骤对应表
     *
     * @param dishId 菜品ID
     */
    private void cascadeDeleteBranchByDishId(Long dishId) {
        // 1. 软删除其所有做法分支
        List<DietDishCookingBranch> branches = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDishId, dishId)
                .list();
        if (CollUtil.isNotEmpty(branches)) {
            List<Long> branchIds = branches.stream().map(DietDishCookingBranch::getBranchId).toList();
            dishCookingBranchService.removeBatchByIds(branchIds);
        }

        // 2. 同步软删除其食材与步骤配方关联
        List<DietDishIngredient> ingredients = dishIngredientService.lambdaQuery()
                .eq(DietDishIngredient::getDishId, dishId)
                .list();
        if (CollUtil.isNotEmpty(ingredients)) {
            List<Long> ingIds = ingredients.stream().map(DietDishIngredient::getRelationId).toList();
            dishIngredientService.removeBatchByIds(ingIds);
        }

        List<DietDishStepRelation> steps = dishStepRelationService.lambdaQuery()
                .eq(DietDishStepRelation::getDishId, dishId)
                .list();
        if (CollUtil.isNotEmpty(steps)) {
            List<Long> stepIds = steps.stream().map(DietDishStepRelation::getRelationId).toList();
            dishStepRelationService.removeBatchByIds(stepIds);
        }
    }

    /**
     * 批量或单条软删除特定做法分支。如果该菜谱已无其他有效分支，则同步软删除菜谱主表。
     *
     * @param dto 批量删除入参 DTO
     * @return 删除是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBranches(BaseDeleteDTO dto) {
        if (Objects.isNull(dto)) {
            return false;
        }
        Set<Long> ids = dto.allIds();
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        List<DietDishCookingBranch> branches = dishCookingBranchService.listByIds(ids);
        if (CollUtil.isNotEmpty(branches)) {
            List<Long> branchIds = branches.stream().map(DietDishCookingBranch::getBranchId).toList();
            dishCookingBranchService.removeBatchByIds(branchIds);

            dishIngredientService.lambdaUpdate()
                    .in(DietDishIngredient::getBranchId, branchIds)
                    .remove();

            dishStepRelationService.lambdaUpdate()
                    .in(DietDishStepRelation::getBranchId, branchIds)
                    .remove();

            for (DietDishCookingBranch branch : branches) {
                checkAndCascadeDeleteDish(branch.getDishId());
            }
            return true;
        }
        return false;
    }

    /**
     * 检查如果该菜谱下已无有效做法分支，则级联软删除菜谱主记录
     *
     * @param dishId 菜品ID
     */
    private void checkAndCascadeDeleteDish(Long dishId) {
        long activeCount = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDishId, dishId)
                .count();
        if (activeCount == 0) {
            DietDish dish = this.baseMapper.selectById(dishId);
            if (Objects.nonNull(dish)) {
                this.removeById(dishId);
            }
        }
    }
}
