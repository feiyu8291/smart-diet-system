package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietDishIngredientMapper;
import com.diet.modules.biz.mapper.DietDishStepRelationMapper;
import com.diet.modules.biz.mapper.DietFamilyMealPlanMapper;
import com.diet.modules.biz.model.entity.*;
import com.diet.modules.biz.model.vo.*;
import lombok.RequiredArgsConstructor;
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
    public List<DietDishBranchVO> getRecommendations(com.diet.modules.biz.model.po.DietMealRecommendQueryPO po) {
        if (po == null || po.getGroupId() == null || po.getTargetDate() == null) {
            return new ArrayList<>();
        }
        LocalDate date = LocalDate.parse(po.getTargetDate());
        List<DietDishCookingBranch> branches = generateRecommendedMeal(po.getGroupId(), date, po.getMealPeriod(), po.getDietMode(), po.getLimit());
        return branches.stream().map(b -> {
            DietDishBranchVO vo = new DietDishBranchVO();
            BeanUtil.copyProperties(b, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 生成家庭联合配餐推荐 (以做法分支为颗粒度)
     */
    public List<DietDishCookingBranch> generateRecommendedMeal(Long groupId, LocalDate targetDate, Integer mealPeriod, Integer dietMode, int limit) {
        List<DietDishCookingBranch> allBranches = dishCookingBranchService.lambdaQuery()
                .eq(DietDishCookingBranch::getDietMode, dietMode)
                .eq(DietDishCookingBranch::getDelFlag, 0)
                .list();
        if (allBranches.isEmpty()) {
            return new ArrayList<>();
        }

        DietFamilyGroup group = familyGroupService.getById(groupId);
        int cooldownDays = (group != null) ? group.getCooldownDays() : 7;

        List<DietUserHealthProfile> profiles = userHealthProfileService.lambdaQuery()
                .eq(DietUserHealthProfile::getGroupId, groupId)
                .eq(DietUserHealthProfile::getDelFlag, 0)
                .list();

        Set<String> cooledCuisines = getCooledCuisines(groupId, targetDate, cooldownDays);
        Long cookUserId = getCookUserId(profiles);

        Set<Long> skilledDishIds = getSkilledDishIds(cookUserId);
        Map<Long, DietCookDishStat> statMap = getCookDishStatMap(cookUserId);
        Set<String> skilledCuisines = getSkilledCuisines(cookUserId, skilledDishIds);

        List<Long> profileIds = profiles.stream().map(DietUserHealthProfile::getProfileId).collect(Collectors.toList());
        List<DietUserWishDish> wishes = getWishes(profileIds);
        Map<Long, Integer> dislikeMap = getDislikeMap(profileIds);

        Map<DietDishCookingBranch, Double> branchWeights = new HashMap<>();
        for (DietDishCookingBranch branch : allBranches) {
            double w = calculateBranchWeight(branch, targetDate, cooledCuisines, wishes, skilledDishIds, skilledCuisines, statMap, dislikeMap);
            if (w > 0.0) {
                branchWeights.put(branch, w);
            }
        }

        // 补足推荐限制数量
        if (branchWeights.size() < limit) {
            fillBackupBranches(branchWeights, allBranches, targetDate, wishes, skilledDishIds, skilledCuisines, statMap, dislikeMap);
        }

        return com.diet.modules.common.util.WeightedRandomUtil.selectWithoutReplacement(branchWeights, limit);
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
                .eq(DietFamilyMealPlan::getDelFlag, 0)
                .list();

        if (pastPlans.isEmpty()) {
            return cooledCuisines;
        }

        List<Long> planIds = pastPlans.stream().map(DietFamilyMealPlan::getMealPlanId).collect(Collectors.toList());
        List<DietFamilyMealPlanDish> pastRelations = familyMealPlanDishService.lambdaQuery()
                .in(DietFamilyMealPlanDish::getMealPlanId, planIds)
                .eq(DietFamilyMealPlanDish::getDelFlag, 0)
                .list();

        if (!pastRelations.isEmpty()) {
            List<Long> pastBranchIds = pastRelations.stream().map(DietFamilyMealPlanDish::getBranchId).collect(Collectors.toList());
            List<DietDishCookingBranch> pastBranches = dishCookingBranchService.listByIds(pastBranchIds);
            cooledCuisines = pastBranches.stream().map(DietDishCookingBranch::getCuisineType).collect(Collectors.toSet());
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
        if (cookUserId == null) {
            return new HashSet<>();
        }
        List<DietCookSkilledDish> skilledDishes = cookSkilledDishService.lambdaQuery()
                .eq(DietCookSkilledDish::getUserId, cookUserId)
                .eq(DietCookSkilledDish::getDelFlag, 0)
                .list();
        return skilledDishes.stream().map(DietCookSkilledDish::getDishId).collect(Collectors.toSet());
    }

    private Map<Long, DietCookDishStat> getCookDishStatMap(Long cookUserId) {
        Map<Long, DietCookDishStat> statMap = new HashMap<>();
        if (cookUserId == null) {
            return statMap;
        }
        List<DietCookDishStat> stats = cookDishStatService.lambdaQuery()
                .eq(DietCookDishStat::getUserId, cookUserId)
                .eq(DietCookDishStat::getDelFlag, 0)
                .list();
        for (DietCookDishStat stat : stats) {
            statMap.put(stat.getDishId(), stat);
        }
        return statMap;
    }

    private Set<String> getSkilledCuisines(Long cookUserId, Set<Long> skilledDishIds) {
        Set<String> skilledCuisines = new HashSet<>();
        if (cookUserId == null || skilledDishIds.isEmpty()) {
            return skilledCuisines;
        }
        List<DietDishCookingBranch> skilledBranches = dishCookingBranchService.lambdaQuery()
                .in(DietDishCookingBranch::getDishId, skilledDishIds).eq(DietDishCookingBranch::getDelFlag, 0).list();

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
                .eq(DietUserWishDish::getDelFlag, 0)
                .list();
    }

    private Map<Long, Integer> getDislikeMap(List<Long> profileIds) {
        Map<Long, Integer> dislikeMap = new HashMap<>();
        if (profileIds.isEmpty()) {
            return dislikeMap;
        }
        List<DietUserDislikeDish> dislikes = userDislikeDishService.lambdaQuery()
                .in(DietUserDislikeDish::getProfileId, profileIds)
                .eq(DietUserDislikeDish::getDelFlag, 0)
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
                wishBoost = (wish.getWishDate() != null && wish.getWishDate().equals(targetDate)) ? Math.max(wishBoost, 3.0) : Math.max(wishBoost,
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

    private void fillBackupBranches(Map<DietDishCookingBranch, Double> branchWeights, List<DietDishCookingBranch> allBranches,
                                    LocalDate targetDate, List<DietUserWishDish> wishes, Set<Long> skilledDishIds,
                                    Set<String> skilledCuisines, Map<Long, DietCookDishStat> statMap, Map<Long, Integer> dislikeMap) {
        for (DietDishCookingBranch branch : allBranches) {
            Long dishId = branch.getDishId();
            Integer dislikeCount = dislikeMap.get(dishId);
            if (dislikeCount != null && dislikeCount >= 3) {
                continue;
            }
            if (!branchWeights.containsKey(branch)) {
                double w = calculateBranchWeight(branch, targetDate, new HashSet<>(), wishes, skilledDishIds, skilledCuisines, statMap, dislikeMap);
                branchWeights.put(branch, w);
            }
        }
    }

    /**
     * 高阶入口：确认并保存膳食计划
     */
    @Transactional(rollbackFor = Exception.class)
    public DietFamilyMealPlanVO saveMealPlan(com.diet.modules.biz.model.dto.DietMealPlanSaveDTO dto) {
        if (dto == null || dto.getGroupId() == null || dto.getTargetDate() == null
                || dto.getMealPeriod() == null || dto.getDietMode() == null || dto.getBranchIds() == null) {
            throw new RuntimeException("参数不完整，保存失败");
        }
        LocalDate date = LocalDate.parse(dto.getTargetDate());
        return saveMealPlan(dto.getGroupId(), date, dto.getMealPeriod(), dto.getDietMode(), dto.getBranchIds());
    }

    /**
     * 确认并保存膳食计划
     */
    @Transactional(rollbackFor = Exception.class)
    public DietFamilyMealPlanVO saveMealPlan(Long groupId, LocalDate targetDate, Integer mealPeriod, Integer dietMode, List<Long> branchIds) {
        DietFamilyMealPlan mealPlan = this.lambdaQuery()
                .eq(DietFamilyMealPlan::getGroupId, groupId)
                .eq(DietFamilyMealPlan::getMealDate, targetDate)
                .eq(DietFamilyMealPlan::getMealPeriod, mealPeriod)
                .eq(DietFamilyMealPlan::getDelFlag, 0)
                .one();

        if (mealPlan != null) {
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
        List<DietFamilyMealPlanDish> rels = branchIds.stream().map(branchId -> {
            DietFamilyMealPlanDish rel = new DietFamilyMealPlanDish();
            rel.setMealPlanId(mealPlanId);
            rel.setBranchId(branchId);
            rel.setDelFlag(0);
            return rel;
        }).collect(Collectors.toList());
        familyMealPlanDishService.saveBatch(rels);
    }

    private void calculateMemberPortions(Long groupId, Long mealPlanId, Integer mealPeriod, List<DietDishCookingBranch> branches) {
        int branchCount = branches.size();
        List<DietUserHealthProfile> profiles = userHealthProfileService.lambdaQuery()
                .eq(DietUserHealthProfile::getGroupId, groupId)
                .eq(DietUserHealthProfile::getDelFlag, 0)
                .list();

        double periodRatio = (mealPeriod == 1 || mealPeriod == 3) ? 0.3 : 0.4;
        List<DietFamilyMealPortion> portionList = new ArrayList<>();

        for (DietUserHealthProfile p : profiles) {
            if (p.getDailyTargetCalories() == null) continue;
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

    private void generateGroceryList(Long mealPlanId, List<DietDishCookingBranch> branches) {
        // 先汇总当前排餐计划中，每个分支被推荐食用的累计推荐质量 (recommendWeight 之和)
        Map<Long, BigDecimal> branchTotalWeights = new HashMap<>();
        for (DietDishCookingBranch branch : branches) {
            List<DietFamilyMealPortion> portions = familyMealPortionService.lambdaQuery()
                    .eq(DietFamilyMealPortion::getMealPlanId, mealPlanId)
                    .eq(DietFamilyMealPortion::getBranchId, branch.getBranchId())
                    .eq(DietFamilyMealPortion::getDelFlag, 0)
                    .list();
            BigDecimal totalWeight = portions.stream()
                    .map(DietFamilyMealPortion::getRecommendWeight)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            branchTotalWeights.put(branch.getBranchId(), totalWeight);
        }

        Map<Long, BigDecimal> ingredientTotalGrams = new HashMap<>();
        for (DietDishCookingBranch branch : branches) {
            BigDecimal branchTotalWeight = branchTotalWeights.get(branch.getBranchId());
            if (branchTotalWeight == null || branchTotalWeight.compareTo(BigDecimal.ZERO) <= 0) continue;

            List<DietDishIngredient> recipeIngredients = dishIngredientService.lambdaQuery()
                    .eq(DietDishIngredient::getBranchId, branch.getBranchId())
                    .eq(DietDishIngredient::getDelFlag, 0)
                    .list();
            if (recipeIngredients.isEmpty()) continue;

            double recipeTotalWeight = recipeIngredients.stream()
                    .mapToDouble(ri -> ri.getUseAmount().doubleValue())
                    .sum();

            if (recipeTotalWeight <= 0) continue;
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
        if (mealPlan == null) return;

        Long groupId = mealPlan.getGroupId();

        List<DietFamilyMealPlanDish> relations = familyMealPlanDishService.lambdaQuery()
                .eq(DietFamilyMealPlanDish::getMealPlanId, mealPlanId)
                .eq(DietFamilyMealPlanDish::getDelFlag, 0)
                .list();
        if (relations.isEmpty()) return;

        List<Long> branchIds = relations.stream().map(DietFamilyMealPlanDish::getBranchId).collect(Collectors.toList());
        List<DietDishCookingBranch> branches = dishCookingBranchService.listByIds(branchIds);
        List<Long> dishIds = branches.stream().map(DietDishCookingBranch::getDishId).distinct().collect(Collectors.toList());

        List<DietUserHealthProfile> profiles = userHealthProfileService.lambdaQuery()
                .eq(DietUserHealthProfile::getGroupId, groupId)
                .eq(DietUserHealthProfile::getDelFlag, 0)
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
        if (cookUserId == null) {
            return;
        }
        for (Long dishId : dishIds) {
            DietCookDishStat stat = cookDishStatService.lambdaQuery()
                    .eq(DietCookDishStat::getUserId, cookUserId)
                    .eq(DietCookDishStat::getDishId, dishId)
                    .one();

            if (stat == null) {
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
        if (dislikes == null || dislikes.isEmpty()) {
            return;
        }
        for (DislikeFeedback feedback : dislikes) {
            DietUserDislikeDish dislike = userDislikeDishService.lambdaQuery()
                    .eq(DietUserDislikeDish::getProfileId, feedback.getProfileId())
                    .eq(DietUserDislikeDish::getDishId, feedback.getDishId())
                    .one();

            if (dislike == null) {
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
    public DietMealDetailVO getMealDetail(com.diet.modules.biz.model.po.DietMealDetailQueryPO po) {
        if (po == null || po.getGroupId() == null || po.getTargetDate() == null) {
            return new DietMealDetailVO();
        }
        LocalDate date = LocalDate.parse(po.getTargetDate());
        return getMealDetail(po.getGroupId(), date, po.getMealPeriod());
    }

    /**
     * 高阶入口：根据 PO 获取联合配餐全天详情
     */
    public DietDayMealDetailVO getDayMealDetail(com.diet.modules.biz.model.po.DietMealDetailQueryPO po) {
        if (po == null || po.getGroupId() == null || po.getTargetDate() == null) {
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
                .eq(DietFamilyMealPlan::getDelFlag, 0)
                .one();

        if (mealPlan == null) {
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
                .eq(DietFamilyMealPlanDish::getDelFlag, 0)
                .list();

        List<DietDishCookingBranch> dishes = new ArrayList<>();
        if (!dishRels.isEmpty()) {
            List<Long> branchIds = dishRels.stream().map(DietFamilyMealPlanDish::getBranchId).collect(Collectors.toList());
            List<DietDishCookingBranch> rawBranches = dishCookingBranchService.listByIds(branchIds);
            for (DietDishCookingBranch b : rawBranches) {
                DietDish dish = dishService.getById(b.getDishId());
                if (dish != null) {
                    b.setBranchName(dish.getDishName() + " (" + b.getBranchName() + ")");
                }
                dishes.add(b);
            }
        }
        return dishes;
    }

    private List<DietPortionVO> getMealPlanPortions(Long mealPlanId) {
        List<DietFamilyMealPortion> portions = familyMealPortionService.lambdaQuery()
                .eq(DietFamilyMealPortion::getMealPlanId, mealPlanId)
                .eq(DietFamilyMealPortion::getDelFlag, 0)
                .list();

        List<DietPortionVO> portionDetails = new ArrayList<>();
        for (DietFamilyMealPortion portion : portions) {
            DietPortionVO item = new DietPortionVO();
            item.setPortionId(portion.getPortionId());
            item.setDishId(portion.getBranchId()); // 做法分支
            item.setRecommendWeight(portion.getRecommendWeight());
            item.setProfileId(portion.getProfileId());

            DietUserHealthProfile profile = userHealthProfileService.getById(portion.getProfileId());
            if (profile != null) {
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
                .eq(DietFamilyMealGrocery::getDelFlag, 0)
                .list();

        List<DietGroceryVO> groceryDetails = new ArrayList<>();
        for (DietFamilyMealGrocery grocery : groceries) {
            DietGroceryVO item = new DietGroceryVO();
            item.setIngredientId(grocery.getIngredientId());
            item.setUseAmount(grocery.getUseAmount());

            DietIngredient ing = ingredientService.getById(grocery.getIngredientId());
            if (ing != null) {
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
        boolean hasMeal = (breakfast != null && Boolean.TRUE.equals(breakfast.getHasMeal()))
                || (lunch != null && Boolean.TRUE.equals(lunch.getHasMeal()))
                || (dinner != null && Boolean.TRUE.equals(dinner.getHasMeal()));

        vo.setHasMeal(hasMeal);
        vo.setBreakfast(breakfast);
        vo.setLunch(lunch);
        vo.setDinner(dinner);

        List<DietGroceryVO> bGroceries = breakfast != null ? breakfast.getGroceries() : null;
        List<DietGroceryVO> lGroceries = lunch != null ? lunch.getGroceries() : null;
        List<DietGroceryVO> dGroceries = dinner != null ? dinner.getGroceries() : null;

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
            if (list == null) {
                continue;
            }
            for (DietGroceryVO grocery : list) {
                if (grocery.getIngredientId() == null) {
                    continue;
                }
                DietGroceryVO existing = mergedMap.get(grocery.getIngredientId());
                if (existing == null) {
                    DietGroceryVO copy = new DietGroceryVO();
                    copy.setIngredientId(grocery.getIngredientId());
                    copy.setIngredientName(grocery.getIngredientName());
                    copy.setMeasureUnit(grocery.getMeasureUnit());
                    copy.setIngredientType(grocery.getIngredientType());
                    copy.setIngredientDesc(grocery.getIngredientDesc());
                    copy.setUseAmount(grocery.getUseAmount() != null ? grocery.getUseAmount() : BigDecimal.ZERO);
                    mergedMap.put(grocery.getIngredientId(), copy);
                } else {
                    BigDecimal amountToAdd = grocery.getUseAmount() != null ? grocery.getUseAmount() : BigDecimal.ZERO;
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
        return (group != null) ? group.getCooldownDays() : 7;
    }

    /**
     * 保存避重冷却天数
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveCooldownDays(Long groupId, Integer cooldownDays) {
        DietFamilyGroup group = familyGroupService.getById(groupId);
        if (group != null) {
            group.setCooldownDays(cooldownDays);
            group.setUpdateTime(LocalDateTime.now());
            return familyGroupService.updateById(group);
        }
        return false;
    }

    /**
     * 不喜欢的菜品反馈 DTO
     */
    public static class DislikeFeedback {
        private Long profileId;
        private Long dishId;

        public Long getProfileId() {
            return profileId;
        }

        public void setProfileId(Long profileId) {
            this.profileId = profileId;
        }

        public Long getDishId() {
            return dishId;
        }

        public void setDishId(Long dishId) {
            this.dishId = dishId;
        }
    }
}
