package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietDishIngredientMapper;
import com.diet.modules.biz.mapper.DietDishStepRelationMapper;
import com.diet.modules.biz.mapper.DietFamilyMealPlanMapper;
import com.diet.modules.biz.model.entity.*;
import com.diet.modules.biz.model.po.DietMealDetailQueryPO;
import com.diet.modules.biz.model.po.DietMealRecommendQueryPO;
import com.diet.modules.biz.model.po.DietMemberMealPortionQueryPO;
import com.diet.modules.biz.model.vo.*;
import com.diet.modules.common.constant.CharacterConstant;
import com.diet.modules.common.constant.DietBizConstant;
import com.diet.modules.common.util.WeightedRandomUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 联合配餐服务类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
@RequiredArgsConstructor
public class DietFamilyMealPlanService extends ServiceImpl<DietFamilyMealPlanMapper, DietFamilyMealPlan> {

    // 只注入本模块的主表 Mapper
    private final DietFamilyMealPlanMapper familyMealPlanMapper;

    // 引入其他相关实体的 Service
    private final DietDishService dishService;
    private final DietFamilyGroupService familyGroupService;
    private final DietFamilyMealPlanDishService familyMealPlanDishService;
    private final DietFamilyMealPortionService familyMealPortionService;
    private final DietFamilyMealGroceryService familyMealGroceryService;
    private final DietDishIngredientService dishIngredientService;
    private final DietCookSkilledDishService cookSkilledDishService;
    private final DietCookDishStatService cookDishStatService;
    private final DietUserWishDishService userWishDishService;
    private final DietUserDislikeDishService userDislikeDishService;
    private final DietUserHealthProfileService userHealthProfileService;
    private final DietIngredientService ingredientService;
    private final DietDishCookingBranchService dishCookingBranchService;
    private final DietDishIngredientMapper dishIngredientMapper;
    private final DietDishStepRelationMapper dishStepRelationMapper;

    /**
     * 高阶入口：生成家庭联合配餐推荐并转换为 VO
     */
    public List<DietDishBranchVO> getRecommendations(DietMealRecommendQueryPO po) {
        if (Objects.isNull(po) || Objects.isNull(po.getGroupId()) || Objects.isNull(po.getTargetDate())) {
            return new ArrayList<>();
        }
        LocalDate date = LocalDate.parse(po.getTargetDate());
        List<DietDishCookingBranch> branches = generateRecommendedMeal(po.getGroupId(), date, po.getMealPeriod(),
                po.getDietMode(), po.getLimit());
        return branches.stream().map(b -> {
            DietDishBranchVO vo = new DietDishBranchVO();
            BeanUtil.copyProperties(b, vo);
            return vo;
        }).toList();
    }

    private static class RecommendContext {
        private final DietFamilyGroup group;
        private final List<DietUserHealthProfile> profiles;
        private final Set<String> cooledCuisines;
        private final Set<Long> skilledDishIds;
        private final Map<Long, DietCookDishStat> statMap;
        private final Set<String> skilledCuisines;
        private final List<DietUserWishDish> wishes;
        private final Map<Long, Integer> dislikeMap;

        public RecommendContext(DietFamilyGroup group, List<DietUserHealthProfile> profiles,
                                Set<String> cooledCuisines, Set<Long> skilledDishIds,
                                Map<Long, DietCookDishStat> statMap, Set<String> skilledCuisines,
                                List<DietUserWishDish> wishes, Map<Long, Integer> dislikeMap) {
            this.group = group;
            this.profiles = profiles;
            this.cooledCuisines = cooledCuisines;
            this.skilledDishIds = skilledDishIds;
            this.statMap = statMap;
            this.skilledCuisines = skilledCuisines;
            this.wishes = wishes;
            this.dislikeMap = dislikeMap;
        }
    }

    private RecommendContext loadRecommendContext(Long groupId, LocalDate targetDate) {
        DietFamilyGroup group = familyGroupService.getById(groupId);
        int cooldownDays = Objects.nonNull(group) ? group.getCooldownDays() : 7;

        List<DietUserHealthProfile> profiles = userHealthProfileService.lambdaQuery()
                .eq(DietUserHealthProfile::getGroupId, groupId)
                .list();

        Set<String> cooledCuisines = getCooledCuisines(groupId, targetDate, cooldownDays);
        Long cookUserId = getCookUserId(profiles);

        Set<Long> skilledDishIds = getSkilledDishIds(cookUserId);
        Map<Long, DietCookDishStat> statMap = getCookDishStatMap(cookUserId);
        Set<String> skilledCuisines = getSkilledCuisines(cookUserId, skilledDishIds);

        List<Long> profileIds = profiles.stream().map(DietUserHealthProfile::getProfileId).toList();
        List<DietUserWishDish> wishes = getWishes(profileIds);
        Map<Long, Integer> dislikeMap = getDislikeMap(profileIds);

        return new RecommendContext(group, profiles, cooledCuisines, skilledDishIds, statMap, skilledCuisines, wishes,
                dislikeMap);
    }

    private Map<DietDishCookingBranch, Double> calculateBranchWeights(RecommendContext context,
                                                                      List<DietDishCookingBranch> allBranches,
                                                                      LocalDate targetDate) {
        Map<DietDishCookingBranch, Double> branchWeights = new HashMap<>();
        for (DietDishCookingBranch branch : allBranches) {
            double w = calculateBranchWeight(branch, targetDate, context.cooledCuisines,
                    context.wishes, context.skilledDishIds, context.skilledCuisines,
                    context.statMap, context.dislikeMap);
            if (w > 0.0) {
                branchWeights.put(branch, w);
            }
        }
        return branchWeights;
    }

    /**
     * 生成家庭联合配餐推荐 (以做法分支为颗粒度)
     */
    public List<DietDishCookingBranch> generateRecommendedMeal(Long groupId, LocalDate targetDate, Integer mealPeriod,
                                                               Integer dietMode, int limit) {
        List<DietDishCookingBranch> allBranches = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDietMode, dietMode)
                .list();
        if (allBranches.isEmpty()) {
            return new ArrayList<>();
        }

        RecommendContext context = loadRecommendContext(groupId, targetDate);
        Map<DietDishCookingBranch, Double> branchWeights = calculateBranchWeights(context, allBranches, targetDate);

        // 补足推荐限制数量
        if (branchWeights.size() < limit) {
            fillBackupBranches(branchWeights, allBranches, targetDate, context.wishes,
                    context.skilledDishIds, context.skilledCuisines, context.statMap, context.dislikeMap);
        }

        return WeightedRandomUtil.selectWithoutReplacement(branchWeights, limit);
    }

    private Set<String> getCooledCuisines(Long groupId, LocalDate targetDate, int cooldownDays) {
        Set<String> cooledCuisines = new HashSet<>();
        if (cooldownDays <= 0) {
            return cooledCuisines;
        }
        LocalDate startDate = targetDate.minusDays(cooldownDays);
        LocalDate endDate = targetDate.minusDays(1);

        List<DietFamilyMealPlan> pastPlans = this.lambdaQuery()
                .eq(DietFamilyMealPlan::getGroupId, groupId)
                .between(DietFamilyMealPlan::getMealDate, startDate, endDate)
                .list();

        if (pastPlans.isEmpty()) {
            return cooledCuisines;
        }

        List<Long> planIds = pastPlans.stream().map(DietFamilyMealPlan::getMealPlanId).toList();
        List<DietFamilyMealPlanDish> pastRelations = familyMealPlanDishService.lambdaQuery()
                .in(DietFamilyMealPlanDish::getMealPlanId, planIds)
                .list();

        if (!pastRelations.isEmpty()) {
            List<Long> pastBranchIds = pastRelations.stream().map(DietFamilyMealPlanDish::getBranchId).toList();
            List<DietDishCookingBranch> pastBranches = dishCookingBranchService.listByIds(pastBranchIds);
            cooledCuisines = pastBranches.stream().map(DietDishCookingBranch::getCuisineType)
                    .collect(Collectors.toSet());
        }
        return cooledCuisines;
    }

    private Long getCookUserId(List<DietUserHealthProfile> profiles) {
        for (DietUserHealthProfile p : profiles) {
            if (p.getGroupRole() == 1) {
                return p.getUserId();
            }
        }
        return null;
    }

    private Set<Long> getSkilledDishIds(Long cookUserId) {
        if (Objects.isNull(cookUserId)) {
            return new HashSet<>();
        }
        List<DietCookSkilledDish> skilledDishes = cookSkilledDishService.lambdaQuery()
                .eq(DietCookSkilledDish::getUserId, cookUserId)
                .list();
        return skilledDishes.stream().map(DietCookSkilledDish::getDishId).collect(Collectors.toSet());
    }

    private Map<Long, DietCookDishStat> getCookDishStatMap(Long cookUserId) {
        Map<Long, DietCookDishStat> statMap = new HashMap<>();
        if (Objects.isNull(cookUserId)) {
            return statMap;
        }
        List<DietCookDishStat> stats = cookDishStatService.lambdaQuery()
                .eq(DietCookDishStat::getUserId, cookUserId)
                .list();
        for (DietCookDishStat stat : stats) {
            statMap.put(stat.getDishId(), stat);
        }
        return statMap;
    }

    private Set<String> getSkilledCuisines(Long cookUserId, Set<Long> skilledDishIds) {
        Set<String> skilledCuisines = new HashSet<>();
        if (Objects.isNull(cookUserId) || skilledDishIds.isEmpty()) {
            return skilledCuisines;
        }
        List<DietDishCookingBranch> skilledBranches = dishCookingBranchService.lambdaQuery()
                .in(DietDishCookingBranch::getDishId, skilledDishIds).list();

        Map<String, Long> cuisineCounts = skilledBranches.stream()
                .collect(Collectors.groupingBy(DietDishCookingBranch::getCuisineType, Collectors.counting()));
        long totalSkilled = skilledBranches.size();
        for (Map.Entry<String, Long> entry : cuisineCounts.entrySet()) {
            double ratio = (double) entry.getValue() / totalSkilled;
            if (ratio >= 0.30 || entry.getValue() >= 2) {
                skilledCuisines.add(entry.getKey());
            }
        }
        return skilledCuisines;
    }

    private List<DietUserWishDish> getWishes(List<Long> profileIds) {
        if (profileIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userWishDishService.lambdaQuery()
                .in(DietUserWishDish::getProfileId, profileIds)
                .list();
    }

    private Map<Long, Integer> getDislikeMap(List<Long> profileIds) {
        Map<Long, Integer> dislikeMap = new HashMap<>();
        if (profileIds.isEmpty()) {
            return dislikeMap;
        }
        List<DietUserDislikeDish> dislikes = userDislikeDishService.lambdaQuery()
                .in(DietUserDislikeDish::getProfileId, profileIds)
                .list();
        for (DietUserDislikeDish d : dislikes) {
            dislikeMap.put(d.getDishId(), d.getDislikeCount());
        }
        return dislikeMap;
    }

    private double calculateBranchWeight(DietDishCookingBranch branch, LocalDate targetDate, Set<String> cooledCuisines,
                                         List<DietUserWishDish> wishes, Set<Long> skilledDishIds, Set<String> skilledCuisines,
                                         Map<Long, DietCookDishStat> statMap, Map<Long, Integer> dislikeMap) {
        double w = 1.0;
        Long dishId = branch.getDishId();

        if (cooledCuisines.contains(branch.getCuisineType())) {
            w = 0.0;
        }

        double wishBoost = 1.0;
        for (DietUserWishDish wish : wishes) {
            if (wish.getDishId().equals(dishId)) {
                wishBoost = (Objects.nonNull(wish.getWishDate()) && wish.getWishDate().equals(targetDate))
                        ? Math.max(wishBoost, 3.0)
                        : Math.max(
                        wishBoost,
                        2.0);
            }
        }
        w *= wishBoost;

        if (skilledDishIds.contains(dishId)) {
            w *= 1.4;
        }
        if (skilledCuisines.contains(branch.getCuisineType())) {
            w *= 1.2;
        }
        DietCookDishStat stat = statMap.get(dishId);
        if (stat != null) {
            w = (stat.getSignatureFlag() == 1) ? w * 1.5 : w * (1.0 + stat.getCookCount() * 0.05);
        }

        Integer dislikeCount = dislikeMap.get(dishId);
        if (dislikeCount != null) {
            if (dislikeCount == 1) {
                w *= 0.5;
            } else if (dislikeCount == 2) {
                w *= 0.2;
            } else if (dislikeCount >= 3) {
                w = 0.0;
            }
        }
        return w;
    }

    private void fillBackupBranches(Map<DietDishCookingBranch, Double> branchWeights,
                                    List<DietDishCookingBranch> allBranches,
                                    LocalDate targetDate, List<DietUserWishDish> wishes, Set<Long> skilledDishIds,
                                    Set<String> skilledCuisines, Map<Long, DietCookDishStat> statMap, Map<Long, Integer> dislikeMap) {
        for (DietDishCookingBranch branch : allBranches) {
            Long dishId = branch.getDishId();
            Integer dislikeCount = dislikeMap.get(dishId);
            if (dislikeCount != null && dislikeCount >= 3) {
                continue;
            }
            if (!branchWeights.containsKey(branch)) {
                double w = calculateBranchWeight(branch, targetDate, new HashSet<>(), wishes, skilledDishIds,
                        skilledCuisines, statMap, dislikeMap);
                branchWeights.put(branch, w);
            }
        }
    }

    /**
     * 高阶入口：确认并保存膳食计划
     */
    @Transactional(rollbackFor = Exception.class)
    public DietFamilyMealPlanVO saveMealPlan(com.diet.modules.biz.model.dto.DietMealPlanSaveDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getGroupId()) || Objects.isNull(dto.getTargetDate())
                || Objects.isNull(dto.getMealPeriod()) || Objects.isNull(dto.getDietMode())
                || Objects.isNull(dto.getBranchIds())) {
            throw new RuntimeException("参数不完整，保存失败");
        }
        LocalDate date = LocalDate.parse(dto.getTargetDate());
        return saveMealPlan(dto.getGroupId(), date, dto.getMealPeriod(), dto.getDietMode(), dto.getBranchIds());
    }

    /**
     * 确认并保存膳食计划
     */
    @Transactional(rollbackFor = Exception.class)
    public DietFamilyMealPlanVO saveMealPlan(Long groupId, LocalDate targetDate, Integer mealPeriod, Integer dietMode,
                                             List<Long> branchIds) {
        DietFamilyMealPlan mealPlan = this.lambdaQuery()
                .eq(DietFamilyMealPlan::getGroupId, groupId)
                .eq(DietFamilyMealPlan::getMealDate, targetDate)
                .eq(DietFamilyMealPlan::getMealPeriod, mealPeriod)
                .one();

        if (Objects.nonNull(mealPlan)) {
            mealPlan.setMealDietMode(dietMode);
            mealPlan.setUpdateTime(LocalDateTime.now());
            this.updateById(mealPlan);
            cleanOldMealPlanRelations(mealPlan.getMealPlanId());
        } else {
            mealPlan = new DietFamilyMealPlan();
            mealPlan.setGroupId(groupId);
            mealPlan.setMealDate(targetDate);
            mealPlan.setMealPeriod(mealPeriod);
            mealPlan.setMealDietMode(dietMode);
            mealPlan.setDelFlag(0);
            this.save(mealPlan);
        }

        Long mealPlanId = mealPlan.getMealPlanId();

        // 插入配餐做法分支关系
        saveMealPlanDishes(mealPlanId, branchIds);

        // 测算各家庭成员的目标食量并写入 portion
        List<DietDishCookingBranch> branches = dishCookingBranchService.listByIds(branchIds);
        calculateMemberPortions(groupId, mealPlanId, mealPeriod, branches);

        // 合并生成买菜超市 grocery 清单
        generateGroceryList(mealPlanId, branches);

        DietFamilyMealPlanVO vo = new DietFamilyMealPlanVO();
        BeanUtil.copyProperties(mealPlan, vo);
        return vo;
    }

    private void cleanOldMealPlanRelations(Long mealPlanId) {
        familyMealPlanDishService.lambdaUpdate()
                .eq(DietFamilyMealPlanDish::getMealPlanId, mealPlanId)
                .remove();

        familyMealPortionService.lambdaUpdate()
                .eq(DietFamilyMealPortion::getMealPlanId, mealPlanId)
                .remove();

        familyMealGroceryService.lambdaUpdate()
                .eq(DietFamilyMealGrocery::getMealPlanId, mealPlanId)
                .remove();
    }

    private void saveMealPlanDishes(Long mealPlanId, List<Long> branchIds) {
        List<DietDishCookingBranch> branches = dishCookingBranchService.listByIds(branchIds);
        Map<Long, Long> branchToDishMap = branches.stream()
                .collect(Collectors.toMap(DietDishCookingBranch::getBranchId, DietDishCookingBranch::getDishId, (d1, d2) -> d1));

        List<DietFamilyMealPlanDish> rels = branchIds.stream().map(branchId -> {
            DietFamilyMealPlanDish rel = new DietFamilyMealPlanDish();
            rel.setMealPlanId(mealPlanId);
            rel.setBranchId(branchId);
            rel.setDishId(branchToDishMap.getOrDefault(branchId, 0L));
            rel.setCookFlag(0);
            rel.setDelFlag(0);
            return rel;
        }).toList();
        familyMealPlanDishService.saveBatch(rels);
    }

    private void calculateMemberPortions(Long groupId, Long mealPlanId, Integer mealPeriod,
                                         List<DietDishCookingBranch> branches) {
        int branchCount = branches.size();
        List<DietUserHealthProfile> profiles = userHealthProfileService.lambdaQuery()
                .eq(DietUserHealthProfile::getGroupId, groupId)
                .list();

        double periodRatio = (mealPeriod == 1 || mealPeriod == 3) ? 0.3 : 0.4;
        List<DietFamilyMealPortion> portionList = new ArrayList<>();

        for (DietUserHealthProfile p : profiles) {
            if (Objects.isNull(p.getDailyTargetCalories())) {
                continue;
            }
            double mealCalBudget = p.getDailyTargetCalories().doubleValue() * periodRatio;

            for (DietDishCookingBranch branch : branches) {
                double portionWeight = 0.0;
                if (branch.getCalories().doubleValue() > 0 && branchCount > 0) {
                    portionWeight = (mealCalBudget * 100) / (branch.getCalories().doubleValue() * branchCount);
                }

                BigDecimal weightBD = BigDecimal.valueOf(portionWeight).setScale(2, RoundingMode.HALF_UP);

                DietFamilyMealPortion portion = new DietFamilyMealPortion();
                portion.setMealPlanId(mealPlanId);
                portion.setProfileId(p.getProfileId());
                portion.setBranchId(branch.getBranchId());
                portion.setRecommendWeight(weightBD);
                portionList.add(portion);
            }
        }
        if (!portionList.isEmpty()) {
            familyMealPortionService.saveBatch(portionList);
        }
    }

    private Map<Long, BigDecimal> calculateBranchTotalWeights(Long mealPlanId, List<DietDishCookingBranch> branches) {
        Map<Long, BigDecimal> branchTotalWeights = new HashMap<>();
        for (DietDishCookingBranch branch : branches) {
            List<DietFamilyMealPortion> portions = familyMealPortionService.lambdaQuery()
                    .eq(DietFamilyMealPortion::getMealPlanId, mealPlanId)
                    .eq(DietFamilyMealPortion::getBranchId, branch.getBranchId())
                    .list();
            BigDecimal totalWeight = portions.stream()
                    .map(DietFamilyMealPortion::getRecommendWeight)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            branchTotalWeights.put(branch.getBranchId(), totalWeight);
        }
        return branchTotalWeights;
    }

    private void generateGroceryList(Long mealPlanId, List<DietDishCookingBranch> branches) {
        Map<Long, BigDecimal> branchTotalWeights = calculateBranchTotalWeights(mealPlanId, branches);
        Map<Long, BigDecimal> ingredientTotalGrams = new HashMap<>();

        for (DietDishCookingBranch branch : branches) {
            BigDecimal branchTotalWeight = branchTotalWeights.get(branch.getBranchId());
            if (Objects.isNull(branchTotalWeight) || branchTotalWeight.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            List<DietDishIngredient> recipeIngredients = dishIngredientService.lambdaQuery()
                    .eq(DietDishIngredient::getBranchId, branch.getBranchId())
                    .list();
            if (recipeIngredients.isEmpty()) {
                continue;
            }

            double recipeTotalWeight = recipeIngredients.stream()
                    .mapToDouble(ri -> ri.getUseAmount().doubleValue())
                    .sum();

            if (recipeTotalWeight <= 0) {
                continue;
            }
            double scale = branchTotalWeight.doubleValue() / recipeTotalWeight;

            for (DietDishIngredient ri : recipeIngredients) {
                double neededGram = ri.getUseAmount().doubleValue() * scale;
                BigDecimal currentNeeded = ingredientTotalGrams.getOrDefault(ri.getIngredientId(), BigDecimal.ZERO);
                ingredientTotalGrams.put(ri.getIngredientId(), currentNeeded.add(BigDecimal.valueOf(neededGram)));
            }
        }

        List<DietFamilyMealGrocery> groceryList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : ingredientTotalGrams.entrySet()) {
            DietFamilyMealGrocery grocery = new DietFamilyMealGrocery();
            grocery.setMealPlanId(mealPlanId);
            grocery.setIngredientId(entry.getKey());
            grocery.setUseAmount(entry.getValue().setScale(2, RoundingMode.HALF_UP));
            groceryList.add(grocery);
        }
        if (!groceryList.isEmpty()) {
            familyMealGroceryService.saveBatch(groceryList);
        }
    }

    /**
     * 确认就餐打卡
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeMeal(Long mealPlanId, List<DislikeFeedback> dislikes) {
        DietFamilyMealPlan mealPlan = familyMealPlanMapper.selectById(mealPlanId);
        if (Objects.isNull(mealPlan)) {
            return;
        }

        Long groupId = mealPlan.getGroupId();

        List<DietFamilyMealPlanDish> relations = familyMealPlanDishService.lambdaQuery()
                .eq(DietFamilyMealPlanDish::getMealPlanId, mealPlanId)
                .list();
        if (relations.isEmpty()) {
            return;
        }

        List<Long> dishIds = relations.stream()
                .map(DietFamilyMealPlanDish::getDishId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<DietUserHealthProfile> profiles = userHealthProfileService.lambdaQuery()
                .eq(DietUserHealthProfile::getGroupId, groupId)
                .list();

        Long cookUserId = getCookUserId(profiles);

        // 1. 更新做饭人拿手菜统计
        updateCookDishStats(cookUserId, dishIds);

        // 2. 插入忌口反馈
        recordDislikesFeedback(groupId, dislikes);

        // 3. 清理已达成的意向
        clearFulfilledWishes(profiles, dishIds);
    }

    private void updateCookDishStats(Long cookUserId, List<Long> dishIds) {
        if (Objects.isNull(cookUserId)) {
            return;
        }
        for (Long dishId : dishIds) {
            DietCookDishStat stat = cookDishStatService.lambdaQuery()
                    .eq(DietCookDishStat::getUserId, cookUserId)
                    .eq(DietCookDishStat::getDishId, dishId)
                    .one();

            if (Objects.isNull(stat)) {
                stat = new DietCookDishStat();
                stat.setUserId(cookUserId);
                stat.setDishId(dishId);
                stat.setCookCount(1);
                stat.setSignatureFlag(0);
                cookDishStatService.save(stat);
            } else {
                int count = stat.getCookCount() + 1;
                stat.setCookCount(count);
                if (count >= 5 && stat.getSignatureFlag() == 0) {
                    stat.setSignatureFlag(1);
                    checkAndAddSkilledDish(cookUserId, dishId);
                }
                stat.setUpdateTime(LocalDateTime.now());
                cookDishStatService.updateById(stat);
            }
        }
    }

    private void checkAndAddSkilledDish(Long cookUserId, Long dishId) {
        boolean skillExists = cookSkilledDishService.lambdaQuery()
                .eq(DietCookSkilledDish::getUserId, cookUserId)
                .eq(DietCookSkilledDish::getDishId, dishId)
                .exists();
        if (!skillExists) {
            DietCookSkilledDish skill = new DietCookSkilledDish();
            skill.setUserId(cookUserId);
            skill.setDishId(dishId);
            cookSkilledDishService.save(skill);
        }
    }

    private void recordDislikesFeedback(Long groupId, List<DislikeFeedback> dislikes) {
        if (Objects.isNull(dislikes) || dislikes.isEmpty()) {
            return;
        }
        for (DislikeFeedback feedback : dislikes) {
            DietUserDislikeDish dislike = userDislikeDishService.lambdaQuery()
                    .eq(DietUserDislikeDish::getProfileId, feedback.getProfileId())
                    .eq(DietUserDislikeDish::getDishId, feedback.getDishId())
                    .one();

            if (Objects.isNull(dislike)) {
                dislike = new DietUserDislikeDish();
                dislike.setProfileId(feedback.getProfileId());
                dislike.setGroupId(groupId);
                dislike.setDishId(feedback.getDishId());
                dislike.setDislikeCount(1);
                userDislikeDishService.save(dislike);
            } else {
                dislike.setDislikeCount(dislike.getDislikeCount() + 1);
                dislike.setUpdateTime(LocalDateTime.now());
                userDislikeDishService.updateById(dislike);
            }
        }
    }

    private void clearFulfilledWishes(List<DietUserHealthProfile> profiles, List<Long> dishIds) {
        for (DietUserHealthProfile p : profiles) {
            userWishDishService.lambdaUpdate()
                    .eq(DietUserWishDish::getProfileId, p.getProfileId())
                    .in(DietUserWishDish::getDishId, dishIds)
                    .remove();
        }
    }

    /**
     * 高阶入口：根据 PO 获取联合配餐详情
     */
    public DietMealDetailVO getMealDetail(DietMealDetailQueryPO po) {
        if (Objects.isNull(po) || Objects.isNull(po.getGroupId()) || Objects.isNull(po.getTargetDate())) {
            return new DietMealDetailVO();
        }
        LocalDate date = LocalDate.parse(po.getTargetDate());
        return getMealDetail(po.getGroupId(), date, po.getMealPeriod());
    }

    /**
     * 高阶入口：根据 PO 获取联合配餐全天详情
     */
    public DietDayMealDetailVO getDayMealDetail(DietMealDetailQueryPO po) {
        if (Objects.isNull(po) || Objects.isNull(po.getGroupId()) || Objects.isNull(po.getTargetDate())) {
            return new DietDayMealDetailVO();
        }
        LocalDate date = LocalDate.parse(po.getTargetDate());
        return getDayMealDetail(po.getGroupId(), date);
    }

    /**
     * 查询某次联合配餐的详情信息 (含配给及食材采购清单)
     */
    public DietMealDetailVO getMealDetail(Long groupId, LocalDate date, Integer mealPeriod) {
        DietMealDetailVO vo = new DietMealDetailVO();

        DietFamilyMealPlan mealPlan = this.lambdaQuery()
                .eq(DietFamilyMealPlan::getGroupId, groupId)
                .eq(DietFamilyMealPlan::getMealDate, date)
                .eq(DietFamilyMealPlan::getMealPeriod, mealPeriod)
                .one();

        if (Objects.isNull(mealPlan)) {
            vo.setHasMeal(false);
            return vo;
        }

        vo.setHasMeal(true);
        vo.setMealPlan(mealPlan);
        Long mealPlanId = mealPlan.getMealPlanId();

        // 1. 获取关联的做法分支列表并装填
        vo.setDishes(getMealPlanCookingBranches(mealPlanId));

        // 2. 获取吃饭分配比例
        vo.setPortions(getMealPlanPortions(mealPlanId));

        // 3. 获取采购清单
        vo.setGroceries(getMealPlanGroceries(mealPlanId));

        return vo;
    }

    private List<DietDishCookingBranch> getMealPlanCookingBranches(Long mealPlanId) {
        List<DietFamilyMealPlanDish> dishRels = familyMealPlanDishService.lambdaQuery()
                .eq(DietFamilyMealPlanDish::getMealPlanId, mealPlanId)
                .list();

        List<DietDishCookingBranch> dishes = new ArrayList<>();
        if (!dishRels.isEmpty()) {
            Map<Long, Integer> branchCookMap = dishRels.stream()
                    .collect(Collectors.toMap(
                            DietFamilyMealPlanDish::getBranchId,
                            rel -> Objects.nonNull(rel.getCookFlag()) ? rel.getCookFlag() : 0,
                            (c1, c2) -> c1
                    ));
            List<Long> branchIds = dishRels.stream().map(DietFamilyMealPlanDish::getBranchId).toList();
            List<DietDishCookingBranch> rawBranches = dishCookingBranchService.listByIds(branchIds);
            for (DietDishCookingBranch b : rawBranches) {
                DietDish dish = dishService.getById(b.getDishId());
                if (Objects.nonNull(dish)) {
                    b.setBranchName(dish.getDishName() + " (" + b.getBranchName() + ")");
                }
                b.setCookFlag(branchCookMap.getOrDefault(b.getBranchId(), 0));
                b.setSteps(dishStepRelationMapper.selectRecipeStepsByBranchId(b.getBranchId()));
                dishes.add(b);
            }
        }
        return dishes;
    }

    private List<DietPortionVO> getMealPlanPortions(Long mealPlanId) {
        List<DietFamilyMealPortion> portions = familyMealPortionService.lambdaQuery()
                .eq(DietFamilyMealPortion::getMealPlanId, mealPlanId)
                .list();

        List<DietPortionVO> portionDetails = new ArrayList<>();
        for (DietFamilyMealPortion portion : portions) {
            DietPortionVO item = new DietPortionVO();
            item.setPortionId(portion.getPortionId());
            item.setDishId(portion.getBranchId()); // 做法分支
            item.setRecommendWeight(portion.getRecommendWeight());
            item.setProfileId(portion.getProfileId());

            DietUserHealthProfile profile = userHealthProfileService.getById(portion.getProfileId());
            if (Objects.nonNull(profile)) {
                item.setMemberName(profile.getMemberName());
                item.setMemberRelation(profile.getMemberRelation());
            }

            portionDetails.add(item);
        }
        return portionDetails;
    }

    private List<DietGroceryVO> getMealPlanGroceries(Long mealPlanId) {
        List<DietFamilyMealGrocery> groceries = familyMealGroceryService.lambdaQuery()
                .eq(DietFamilyMealGrocery::getMealPlanId, mealPlanId)
                .list();

        List<DietGroceryVO> groceryDetails = new ArrayList<>();
        for (DietFamilyMealGrocery grocery : groceries) {
            DietGroceryVO item = new DietGroceryVO();
            item.setIngredientId(grocery.getIngredientId());
            item.setUseAmount(grocery.getUseAmount());

            DietIngredient ing = ingredientService.getById(grocery.getIngredientId());
            if (Objects.nonNull(ing)) {
                item.setIngredientName(ing.getIngredientName());
                item.setMeasureUnit(ing.getMeasureUnit());
                item.setIngredientType(ing.getIngredientType());
                item.setIngredientDesc(ing.getIngredientDesc());
            }
            groceryDetails.add(item);
        }
        return groceryDetails;
    }

    /**
     * 获取全天膳食详情并合并采购清单
     */
    public DietDayMealDetailVO getDayMealDetail(Long groupId, LocalDate date) {
        DietMealDetailVO breakfast = getMealDetail(groupId, date, 1);
        DietMealDetailVO lunch = getMealDetail(groupId, date, 2);
        DietMealDetailVO dinner = getMealDetail(groupId, date, 3);

        DietDayMealDetailVO vo = new DietDayMealDetailVO();
        boolean hasMeal = (Objects.nonNull(breakfast) && Boolean.TRUE.equals(breakfast.getHasMeal()))
                || (Objects.nonNull(lunch) && Boolean.TRUE.equals(lunch.getHasMeal()))
                || (Objects.nonNull(dinner) && Boolean.TRUE.equals(dinner.getHasMeal()));

        vo.setHasMeal(hasMeal);
        vo.setBreakfast(breakfast);
        vo.setLunch(lunch);
        vo.setDinner(dinner);

        List<DietGroceryVO> bGroceries = Objects.nonNull(breakfast) ? breakfast.getGroceries() : null;
        List<DietGroceryVO> lGroceries = Objects.nonNull(lunch) ? lunch.getGroceries() : null;
        List<DietGroceryVO> dGroceries = Objects.nonNull(dinner) ? dinner.getGroceries() : null;

        vo.setDailyGroceries(mergeGroceries(bGroceries, lGroceries, dGroceries));
        return vo;
    }

    /**
     * 合并多餐的食材采购清单
     */
    @SafeVarargs
    private final List<DietGroceryVO> mergeGroceries(List<DietGroceryVO>... groceryLists) {
        Map<Long, DietGroceryVO> mergedMap = new LinkedHashMap<>();
        for (List<DietGroceryVO> list : groceryLists) {
            if (Objects.isNull(list)) {
                continue;
            }
            for (DietGroceryVO grocery : list) {
                if (Objects.isNull(grocery.getIngredientId())) {
                    continue;
                }
                DietGroceryVO existing = mergedMap.get(grocery.getIngredientId());
                if (Objects.isNull(existing)) {
                    DietGroceryVO copy = new DietGroceryVO();
                    copy.setIngredientId(grocery.getIngredientId());
                    copy.setIngredientName(grocery.getIngredientName());
                    copy.setMeasureUnit(grocery.getMeasureUnit());
                    copy.setIngredientType(grocery.getIngredientType());
                    copy.setIngredientDesc(grocery.getIngredientDesc());
                    copy.setUseAmount(
                            Objects.nonNull(grocery.getUseAmount()) ? grocery.getUseAmount() : BigDecimal.ZERO);
                    mergedMap.put(grocery.getIngredientId(), copy);
                } else {
                    BigDecimal amountToAdd = Objects.nonNull(grocery.getUseAmount()) ? grocery.getUseAmount()
                            : BigDecimal.ZERO;
                    existing.setUseAmount(existing.getUseAmount().add(amountToAdd));
                }
            }
        }
        return new ArrayList<>(mergedMap.values());
    }

    /**
     * 获取避重冷却天数
     */
    public Integer getCooldownDays(Long groupId) {
        DietFamilyGroup group = familyGroupService.getById(groupId);
        return Objects.nonNull(group) ? group.getCooldownDays() : 7;
    }

    /**
     * 保存避重冷却天数
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveCooldownDays(Long groupId, Integer cooldownDays) {
        DietFamilyGroup group = familyGroupService.getById(groupId);
        if (Objects.nonNull(group)) {
            group.setCooldownDays(cooldownDays);
            group.setUpdateTime(LocalDateTime.now());
            return familyGroupService.updateById(group);
        }
        return false;
    }

    /**
     * 不喜欢的菜品反馈 DTO
     */
    @Setter
    @Getter
    public static class DislikeFeedback {
        private Long profileId;
        private Long dishId;

    }

    /**
     * 查询成员当天的就餐分量列表 (业务下沉至 Service)
     *
     * @param po 查询请求PO
     * @return 该成员当天的餐次分量列表
     * @author Antigravity
     * @date 2026-06-30
     */
    public List<DietMemberMealPortionVO> getPortionsByMember(DietMemberMealPortionQueryPO po) {
        if (Objects.isNull(po) || Objects.isNull(po.getGroupId()) || Objects.isNull(po.getProfileId())
                || Objects.isNull(po.getDate())) {
            return Collections.emptyList();
        }
        Long groupId = po.getGroupId();
        Long profileId = po.getProfileId();
        LocalDate date = po.getDate();

        DietDayMealDetailVO dayDetail = this.getDayMealDetail(groupId, date);
        if (Objects.isNull(dayDetail)) {
            return Collections.emptyList();
        }
        List<DietMemberMealPortionVO> result = new ArrayList<>();

        if (Objects.nonNull(dayDetail.getBreakfast()) && Boolean.TRUE.equals(dayDetail.getBreakfast().getHasMeal())) {
            DietMemberMealPortionVO breakfastVO = buildMealPortionVO(
                    dayDetail.getBreakfast(),
                    profileId,
                    DietBizConstant.MealPeriod.BREAKFAST.getCode(),
                    DietBizConstant.MealPeriod.BREAKFAST.getName());
            if (Objects.nonNull(breakfastVO)) {
                result.add(breakfastVO);
            }
        }
        if (Objects.nonNull(dayDetail.getLunch()) && Boolean.TRUE.equals(dayDetail.getLunch().getHasMeal())) {
            DietMemberMealPortionVO lunchVO = buildMealPortionVO(
                    dayDetail.getLunch(),
                    profileId,
                    DietBizConstant.MealPeriod.LUNCH.getCode(),
                    DietBizConstant.MealPeriod.LUNCH.getName());
            if (Objects.nonNull(lunchVO)) {
                result.add(lunchVO);
            }
        }
        if (Objects.nonNull(dayDetail.getDinner()) && Boolean.TRUE.equals(dayDetail.getDinner().getHasMeal())) {
            DietMemberMealPortionVO dinnerVO = buildMealPortionVO(
                    dayDetail.getDinner(),
                    profileId,
                    DietBizConstant.MealPeriod.DINNER.getCode(),
                    DietBizConstant.MealPeriod.DINNER.getName());
            if (Objects.nonNull(dinnerVO)) {
                result.add(dinnerVO);
            }
        }

        return result;
    }

    /**
     * 构建单餐的成员膳食分量VO
     *
     * @param detail     餐次详情数据
     * @param profileId  成员健康档案ID
     * @param periodCode 餐次编码
     * @param periodName 餐次名称
     * @return 膳食分量VO
     * @author Antigravity
     * @date 2026-06-30
     */
    private DietMemberMealPortionVO buildMealPortionVO(DietMealDetailVO detail, Long profileId, Integer periodCode,
                                                       String periodName) {
        DietMemberMealPortionVO vo = new DietMemberMealPortionVO();
        Long mealPlanId = Objects.nonNull(detail.getMealPlan()) ? detail.getMealPlan().getMealPlanId() : null;
        vo.setMealPlanId(mealPlanId);
        vo.setPeriodName(periodName);
        vo.setPeriodCode(periodCode);

        Integer dietMode = Objects.nonNull(detail.getMealPlan()) ? detail.getMealPlan().getMealDietMode() : null;
        vo.setDietModeName(DietBizConstant.DietMode.getNameByCode(dietMode));

        List<DietMemberDishPortionVO> dishesList = new ArrayList<>();
        if (CollUtil.isEmpty(detail.getPortions())) {
            vo.setDishes(dishesList);
            return vo;
        }

        for (DietPortionVO portion : detail.getPortions()) {
            if (!Objects.equals(portion.getProfileId(), profileId)) {
                continue;
            }

            DietMemberDishPortionVO dish = new DietMemberDishPortionVO();
            dish.setId(portion.getPortionId());

            DietDishCookingBranch branch = null;
            if (CollUtil.isNotEmpty(detail.getDishes())) {
                branch = detail.getDishes().stream()
                        .filter(b -> Objects.equals(b.getDishId(), portion.getDishId()))
                        .findFirst()
                        .orElse(null);
            }

            if (Objects.isNull(branch)) {
                dish.setName(DietBizConstant.DEFAULT_DISH_NAME);
                dish.setPortion("150" + DietBizConstant.GRAM_UNIT);
                dish.setCalories(BigDecimal.valueOf(150));
                dish.setProtein(BigDecimal.valueOf(10.0));
                dish.setFat(BigDecimal.valueOf(4.0));
                dish.setCarbs(BigDecimal.valueOf(20.0));
                dish.setNote(DietBizConstant.DEFAULT_DISH_NOTE);
                dishesList.add(dish);
                continue;
            }

            String dishName = DietBizConstant.DEFAULT_DISH_NAME;
            DietDish mainDish = dishService.getById(branch.getDishId());
            if (Objects.nonNull(mainDish)) {
                dishName = mainDish.getDishName();
            }

            String displayName = dishName
                    + CharacterConstant.SPACE
                    + CharacterConstant.BRACKET_LEFT_EN
                    + branch.getBranchName()
                    + CharacterConstant.BRACKET_RIGHT_EN;
            dish.setName(displayName);

            BigDecimal weight = portion.getRecommendWeight() != null ? portion.getRecommendWeight()
                    : BigDecimal.valueOf(150);
            dish.setPortion(weight.intValue() + DietBizConstant.GRAM_UNIT);

            double factor = weight.doubleValue() / 100.0;
            BigDecimal calories = branch.getCalories() != null
                    ? branch.getCalories().multiply(BigDecimal.valueOf(factor))
                    : BigDecimal.ZERO;
            BigDecimal protein = branch.getProtein() != null ? branch.getProtein().multiply(BigDecimal.valueOf(factor))
                    : BigDecimal.ZERO;
            BigDecimal fat = branch.getFat() != null ? branch.getFat().multiply(BigDecimal.valueOf(factor))
                    : BigDecimal.ZERO;
            BigDecimal carbs = branch.getCarbs() != null ? branch.getCarbs().multiply(BigDecimal.valueOf(factor))
                    : BigDecimal.ZERO;

            dish.setCalories(calories.setScale(1, RoundingMode.HALF_UP));
            dish.setProtein(protein.setScale(1, RoundingMode.HALF_UP));
            dish.setFat(fat.setScale(1, RoundingMode.HALF_UP));
            dish.setCarbs(carbs.setScale(1, RoundingMode.HALF_UP));
            dish.setNote(DietBizConstant.PORTION_NOTE);
            dishesList.add(dish);
        }

        vo.setDishes(dishesList);
        return vo;
    }

    /**
     * 生成全天三餐膳食推荐 (业务下沉至 Service)
     *
     * @param groupId  家庭组ID
     * @param mealDate 排餐目标日期
     * @param dietMode 建议膳食模式
     * @return 包含早餐/午餐/晚餐的推荐卡片 Map
     * @author Antigravity
     * @date 2026-06-30
     */
    public Map<String, List<Map<String, Object>>> generateRecommend(Long groupId, LocalDate mealDate,
                                                                    Integer dietMode) {
        List<DietDishCookingBranch> breakfastList = generateRecommendedMeal(groupId, mealDate,
                DietBizConstant.MealPeriod.BREAKFAST.getCode(), dietMode, 3);
        List<DietDishCookingBranch> lunchList = generateRecommendedMeal(groupId, mealDate,
                DietBizConstant.MealPeriod.LUNCH.getCode(), dietMode, 3);
        List<DietDishCookingBranch> dinnerList = generateRecommendedMeal(groupId, mealDate,
                DietBizConstant.MealPeriod.DINNER.getCode(), dietMode, 3);

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("breakfast", convertBranchToRecommendList(breakfastList));
        result.put("lunch", convertBranchToRecommendList(lunchList));
        result.put("dinner", convertBranchToRecommendList(dinnerList));
        return result;
    }

    /**
     * 将做法分支列表转换为前端所需的 Map 格式推荐卡片列表
     *
     * @param list 做法分支实体列表
     * @return 包含分支名称、热量与标签的 Map 列表
     * @author Antigravity
     * @date 2026-06-30
     */
    private List<Map<String, Object>> convertBranchToRecommendList(List<DietDishCookingBranch> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> resList = new ArrayList<>();
        List<Long> dishIds = list.stream().map(DietDishCookingBranch::getDishId).distinct()
                .collect(Collectors.toList());
        Map<Long, DietDish> dishMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(dishIds)) {
            dishMap = dishService.listByIds(dishIds).stream()
                    .collect(Collectors.toMap(DietDish::getDishId, d -> d, (d1, d2) -> d1));
        }

        for (DietDishCookingBranch branch : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", branch.getBranchId());

            DietDish mainDish = dishMap.get(branch.getDishId());
            String dishName = Objects.nonNull(mainDish) ? mainDish.getDishName() : DietBizConstant.DEFAULT_DISH_NAME;

            String displayName = dishName
                    + CharacterConstant.SPACE
                    + CharacterConstant.BRACKET_LEFT_EN
                    + branch.getBranchName()
                    + CharacterConstant.BRACKET_RIGHT_EN;
            item.put("name", displayName);
            item.put("calories", Objects.nonNull(branch.getCalories()) ? branch.getCalories().intValue() : 100);

            List<String> tags = new ArrayList<>();
            if (cn.hutool.core.text.CharSequenceUtil.isNotBlank(branch.getCuisineType())) {
                tags.add(branch.getCuisineType());
            }
            if (Objects.nonNull(branch.getDietMode())) {
                if (Objects.equals(branch.getDietMode(), 0)) {
                    tags.add("正常饮食");
                } else if (Objects.equals(branch.getDietMode(), 1)) {
                    tags.add("轻食减脂");
                } else if (Objects.equals(branch.getDietMode(), 2)) {
                    tags.add("放纵餐");
                }
            }
            item.put("tags", tags);
            resList.add(item);
        }
        return resList;
    }

    /**
     * 确认并发布全天配餐计划，触发食材合并与就餐分量核算
     *
     * @param groupId  家庭组ID
     * @param mealDate 发布目标日期
     * @param menuData 三餐配给做法列表 Map
     * @return 发布是否成功
     * @author Antigravity
     * @date 2026-06-30
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishMealPlan(Long groupId, LocalDate mealDate, Map<String, List<Map<String, Object>>> menuData) {
        if (Objects.isNull(menuData)) {
            return false;
        }

        savePeriodMealPlan(groupId, mealDate, DietBizConstant.MealPeriod.BREAKFAST.getCode(),
                menuData.get("breakfast"));
        savePeriodMealPlan(groupId, mealDate, DietBizConstant.MealPeriod.LUNCH.getCode(), menuData.get("lunch"));
        savePeriodMealPlan(groupId, mealDate, DietBizConstant.MealPeriod.DINNER.getCode(), menuData.get("dinner"));

        return true;
    }

    /**
     * 保存单餐次排餐数据并触发膳食分量自动测算
     *
     * @param groupId    家庭组ID
     * @param mealDate   就餐日期
     * @param periodCode 餐次编码
     * @param dishItems  单餐做法分支项列表
     * @author Antigravity
     * @date 2026-06-30
     */
    private void savePeriodMealPlan(Long groupId, LocalDate mealDate, Integer periodCode,
                                    List<Map<String, Object>> dishItems) {
        if (CollUtil.isEmpty(dishItems)) {
            return;
        }

        List<Long> branchIds = new ArrayList<>();
        Integer dietMode = DietBizConstant.DietMode.NORMAL.getCode();

        for (Map<String, Object> item : dishItems) {
            Number idNum = (Number) item.get("id");
            if (Objects.nonNull(idNum)) {
                branchIds.add(idNum.longValue());
            }
        }

        if (CollUtil.isEmpty(branchIds)) {
            return;
        }

        List<DietDishCookingBranch> branches = dishCookingBranchService.listByIds(branchIds);
        if (CollUtil.isNotEmpty(branches)) {
            boolean hasLight = branches.stream()
                    .anyMatch(b -> Objects.equals(b.getDietMode(), DietBizConstant.MealPeriod.BREAKFAST.getCode()));
            boolean hasCheat = branches.stream()
                    .anyMatch(b -> Objects.equals(b.getDietMode(), DietBizConstant.MealPeriod.LUNCH.getCode()));
            if (hasLight) {
                dietMode = DietBizConstant.DietMode.LIGHT.getCode();
            } else if (hasCheat) {
                dietMode = DietBizConstant.DietMode.CHEAT.getCode();
            }
        }

        this.saveMealPlan(groupId, mealDate, periodCode, dietMode, branchIds);
    }

    /**
     * 更新配餐菜品烹饪完成状态
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDishCookFlag(Long mealPlanId, Long branchId, Integer cookFlag) {
        return familyMealPlanDishService.lambdaUpdate()
                .eq(DietFamilyMealPlanDish::getMealPlanId, mealPlanId)
                .eq(DietFamilyMealPlanDish::getBranchId, branchId)
                .set(DietFamilyMealPlanDish::getCookFlag, cookFlag)
                .set(DietFamilyMealPlanDish::getUpdateTime, LocalDateTime.now())
                .update();
    }
}
