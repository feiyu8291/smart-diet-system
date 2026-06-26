package com.diet.modules.biz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.*;
import com.diet.modules.biz.model.entity.*;
import com.diet.modules.biz.model.vo.DietDayMealDetailVO;
import com.diet.modules.biz.model.vo.DietGroceryVO;
import com.diet.modules.biz.model.vo.DietMealDetailVO;
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

    private final DietDishMapper dishMapper;
    private final DietFamilyGroupMapper familyGroupMapper;
    private final DietFamilyMealPlanMapper familyMealPlanMapper;
    private final DietFamilyMealPlanDishMapper familyMealPlanDishMapper;
    private final DietFamilyMealPortionMapper familyMealPortionMapper;
    private final DietFamilyMealGroceryMapper familyMealGroceryMapper;
    private final DietDishIngredientMapper dishIngredientMapper;
    private final DietCookSkilledDishMapper cookSkilledDishMapper;
    private final DietCookDishStatMapper cookDishStatMapper;
    private final DietUserWishDishMapper userWishDishMapper;
    private final DietUserDislikeDishMapper userDislikeDishMapper;
    private final DietUserHealthProfileMapper userHealthProfileMapper;
    private final DietIngredientMapper ingredientMapper;
    private final DietDishCookingBranchMapper dishCookingBranchMapper;

    /**
     * 生成家庭联合配餐推荐 (以做法分支为颗粒度)
     */
    public List<DietDishCookingBranch> generateRecommendedMeal(Long groupId, LocalDate targetDate, Integer mealPeriod, Integer dietMode, int limit) {
        LambdaQueryWrapper<DietDishCookingBranch> branchWrapper = new LambdaQueryWrapper<>();
        branchWrapper.eq(DietDishCookingBranch::getDietMode, dietMode)
                .eq(DietDishCookingBranch::getDelFlag, 0);
        List<DietDishCookingBranch> allBranches = dishCookingBranchMapper.selectList(branchWrapper);
        if (allBranches.isEmpty()) {
            return new ArrayList<>();
        }

        DietFamilyGroup group = familyGroupMapper.selectById(groupId);
        int cooldownDays = (group != null) ? group.getCooldownDays() : 7;

        LambdaQueryWrapper<DietUserHealthProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(DietUserHealthProfile::getGroupId, groupId)
                .eq(DietUserHealthProfile::getDelFlag, 0);
        List<DietUserHealthProfile> profiles = userHealthProfileMapper.selectList(profileWrapper);

        Set<String> cooledCuisines = new HashSet<>();
        if (cooldownDays > 0) {
            LocalDate startDate = targetDate.minusDays(cooldownDays);
            LocalDate endDate = targetDate.minusDays(1);

            LambdaQueryWrapper<DietFamilyMealPlan> pastMealWrapper = new LambdaQueryWrapper<>();
            pastMealWrapper.eq(DietFamilyMealPlan::getGroupId, groupId)
                    .between(DietFamilyMealPlan::getMealDate, startDate, endDate)
                    .eq(DietFamilyMealPlan::getDelFlag, 0);
            List<DietFamilyMealPlan> pastPlans = familyMealPlanMapper.selectList(pastMealWrapper);

            if (!pastPlans.isEmpty()) {
                List<Long> planIds = pastPlans.stream().map(DietFamilyMealPlan::getMealPlanId).collect(Collectors.toList());
                LambdaQueryWrapper<DietFamilyMealPlanDish> pastDishWrapper = new LambdaQueryWrapper<>();
                pastDishWrapper.in(DietFamilyMealPlanDish::getMealPlanId, planIds)
                        .eq(DietFamilyMealPlanDish::getDelFlag, 0);
                List<DietFamilyMealPlanDish> pastRelations = familyMealPlanDishMapper.selectList(pastDishWrapper);

                if (!pastRelations.isEmpty()) {
                    List<Long> pastBranchIds = pastRelations.stream().map(DietFamilyMealPlanDish::getBranchId).collect(Collectors.toList());
                    List<DietDishCookingBranch> pastBranches = dishCookingBranchMapper.selectBatchIds(pastBranchIds);
                    cooledCuisines = pastBranches.stream().map(DietDishCookingBranch::getCuisineType).collect(Collectors.toSet());
                }
            }
        }

        Long cookUserId = null;
        for (DietUserHealthProfile p : profiles) {
            if (p.getGroupRole() == 1) {
                cookUserId = p.getUserId();
                break;
            }
        }

        Set<Long> skilledDishIds = new HashSet<>();
        Map<Long, DietCookDishStat> statMap = new HashMap<>();
        Set<String> skilledCuisines = new HashSet<>();

        if (cookUserId != null) {
            LambdaQueryWrapper<DietCookSkilledDish> skilledWrapper = new LambdaQueryWrapper<>();
            skilledWrapper.eq(DietCookSkilledDish::getUserId, cookUserId)
                    .eq(DietCookSkilledDish::getDelFlag, 0);
            List<DietCookSkilledDish> skilledDishes = cookSkilledDishMapper.selectList(skilledWrapper);
            skilledDishIds = skilledDishes.stream().map(DietCookSkilledDish::getDishId).collect(Collectors.toSet());

            LambdaQueryWrapper<DietCookDishStat> statWrapper = new LambdaQueryWrapper<>();
            statWrapper.eq(DietCookDishStat::getUserId, cookUserId)
                    .eq(DietCookDishStat::getDelFlag, 0);
            List<DietCookDishStat> stats = cookDishStatMapper.selectList(statWrapper);
            for (DietCookDishStat stat : stats) {
                statMap.put(stat.getDishId(), stat);
            }

            if (!skilledDishIds.isEmpty()) {
                LambdaQueryWrapper<DietDishCookingBranch> branchCuisineWrapper = new LambdaQueryWrapper<>();
                branchCuisineWrapper.in(DietDishCookingBranch::getDishId, skilledDishIds).eq(DietDishCookingBranch::getDelFlag, 0);
                List<DietDishCookingBranch> skilledBranches = dishCookingBranchMapper.selectList(branchCuisineWrapper);

                Map<String, Long> cuisineCounts = skilledBranches.stream()
                        .collect(Collectors.groupingBy(DietDishCookingBranch::getCuisineType, Collectors.counting()));
                long totalSkilled = skilledBranches.size();
                for (Map.Entry<String, Long> entry : cuisineCounts.entrySet()) {
                    double ratio = (double) entry.getValue() / totalSkilled;
                    if (ratio >= 0.30 || entry.getValue() >= 2) {
                        skilledCuisines.add(entry.getKey());
                    }
                }
            }
        }

        List<Long> profileIds = profiles.stream().map(DietUserHealthProfile::getProfileId).collect(Collectors.toList());

        List<DietUserWishDish> wishes = new ArrayList<>();
        if (!profileIds.isEmpty()) {
            LambdaQueryWrapper<DietUserWishDish> wishWrapper = new LambdaQueryWrapper<>();
            wishWrapper.in(DietUserWishDish::getProfileId, profileIds)
                    .eq(DietUserWishDish::getDelFlag, 0);
            wishes = userWishDishMapper.selectList(wishWrapper);
        }

        Map<Long, Integer> dislikeMap = new HashMap<>();
        if (!profileIds.isEmpty()) {
            LambdaQueryWrapper<DietUserDislikeDish> dislikeWrapper = new LambdaQueryWrapper<>();
            dislikeWrapper.in(DietUserDislikeDish::getProfileId, profileIds)
                    .eq(DietUserDislikeDish::getDelFlag, 0);
            List<DietUserDislikeDish> dislikes = userDislikeDishMapper.selectList(dislikeWrapper);
            for (DietUserDislikeDish d : dislikes) {
                dislikeMap.put(d.getDishId(), d.getDislikeCount());
            }
        }

        Map<DietDishCookingBranch, Double> branchWeights = new HashMap<>();
        for (DietDishCookingBranch branch : allBranches) {
            double w = 1.0;
            Long dishId = branch.getDishId();

            if (cooledCuisines.contains(branch.getCuisineType())) {
                w = 0.0;
            }

            double wishBoost = 1.0;
            for (DietUserWishDish wish : wishes) {
                if (wish.getDishId().equals(dishId)) {
                    if (wish.getWishDate() != null && wish.getWishDate().equals(targetDate)) {
                        wishBoost = Math.max(wishBoost, 3.0);
                    } else {
                        wishBoost = Math.max(wishBoost, 2.0);
                    }
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
                if (stat.getSignatureFlag() == 1) {
                    w *= 1.5;
                } else if (stat.getCookCount() > 0) {
                    w *= (1.0 + stat.getCookCount() * 0.05);
                }
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

            if (w > 0.0) {
                branchWeights.put(branch, w);
            }
        }

        if (branchWeights.size() < limit) {
            for (DietDishCookingBranch branch : allBranches) {
                Long dishId = branch.getDishId();
                Integer dislikeCount = dislikeMap.get(dishId);
                if (dislikeCount != null && dislikeCount >= 3) {
                    continue;
                }
                if (!branchWeights.containsKey(branch)) {
                    double w = 1.0;
                    double wishBoost = 1.0;
                    for (DietUserWishDish wish : wishes) {
                        if (wish.getDishId().equals(dishId)) {
                            if (wish.getWishDate() != null && wish.getWishDate().equals(targetDate)) {
                                wishBoost = Math.max(wishBoost, 3.0);
                            } else {
                                wishBoost = Math.max(wishBoost, 2.0);
                            }
                        }
                    }
                    w *= wishBoost;
                    if (skilledDishIds.contains(dishId)) w *= 1.4;
                    if (skilledCuisines.contains(branch.getCuisineType())) w *= 1.2;
                    DietCookDishStat stat = statMap.get(dishId);
                    if (stat != null) {
                        if (stat.getSignatureFlag() == 1) w *= 1.5;
                        else w *= (1.0 + stat.getCookCount() * 0.05);
                    }
                    if (dislikeCount != null) {
                        if (dislikeCount == 1) w *= 0.5;
                        else if (dislikeCount == 2) w *= 0.2;
                    }
                    branchWeights.put(branch, w);
                }
            }
        }

        return com.diet.modules.common.util.WeightedRandomUtil.selectWithoutReplacement(branchWeights, limit);
    }

    /**
     * 确认并保存膳食计划
     */
    @Transactional(rollbackFor = Exception.class)
    public DietFamilyMealPlan saveMealPlan(Long groupId, LocalDate targetDate, Integer mealPeriod, Integer dietMode, List<Long> branchIds) {
        LambdaQueryWrapper<DietFamilyMealPlan> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(DietFamilyMealPlan::getGroupId, groupId)
                .eq(DietFamilyMealPlan::getMealDate, targetDate)
                .eq(DietFamilyMealPlan::getMealPeriod, mealPeriod)
                .eq(DietFamilyMealPlan::getDelFlag, 0);
        DietFamilyMealPlan mealPlan = familyMealPlanMapper.selectOne(existQuery);

        if (mealPlan != null) {
            mealPlan.setMealDietMode(dietMode);
            mealPlan.setUpdateTime(LocalDateTime.now());
            familyMealPlanMapper.updateById(mealPlan);

            LambdaQueryWrapper<DietFamilyMealPlanDish> oldDishRel = new LambdaQueryWrapper<>();
            oldDishRel.eq(DietFamilyMealPlanDish::getMealPlanId, mealPlan.getMealPlanId());
            familyMealPlanDishMapper.delete(oldDishRel);

            LambdaQueryWrapper<DietFamilyMealPortion> oldPortions = new LambdaQueryWrapper<>();
            oldPortions.eq(DietFamilyMealPortion::getMealPlanId, mealPlan.getMealPlanId());
            familyMealPortionMapper.delete(oldPortions);

            LambdaQueryWrapper<DietFamilyMealGrocery> oldGroceries = new LambdaQueryWrapper<>();
            oldGroceries.eq(DietFamilyMealGrocery::getMealPlanId, mealPlan.getMealPlanId());
            familyMealGroceryMapper.delete(oldGroceries);
        } else {
            mealPlan = new DietFamilyMealPlan();
            mealPlan.setGroupId(groupId);
            mealPlan.setMealDate(targetDate);
            mealPlan.setMealPeriod(mealPeriod);
            mealPlan.setMealDietMode(dietMode);
            mealPlan.setDelFlag(0);
            familyMealPlanMapper.insert(mealPlan);
        }

        Long mealPlanId = mealPlan.getMealPlanId();

        for (Long branchId : branchIds) {
            DietFamilyMealPlanDish rel = new DietFamilyMealPlanDish();
            rel.setMealPlanId(mealPlanId);
            rel.setBranchId(branchId);
            rel.setDelFlag(0);
            familyMealPlanDishMapper.insert(rel);
        }

        List<DietDishCookingBranch> branches = dishCookingBranchMapper.selectBatchIds(branchIds);
        int branchCount = branches.size();

        LambdaQueryWrapper<DietUserHealthProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(DietUserHealthProfile::getGroupId, groupId)
                .eq(DietUserHealthProfile::getDelFlag, 0);
        List<DietUserHealthProfile> profiles = userHealthProfileMapper.selectList(profileWrapper);

        double periodRatio = (mealPeriod == 1 || mealPeriod == 3) ? 0.3 : 0.4;
        Map<Long, BigDecimal> branchTotalWeights = new HashMap<>();
        for (DietDishCookingBranch branch : branches) {
            branchTotalWeights.put(branch.getBranchId(), BigDecimal.ZERO);
        }

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
                familyMealPortionMapper.insert(portion);

                BigDecimal currentTotal = branchTotalWeights.get(branch.getBranchId());
                branchTotalWeights.put(branch.getBranchId(), currentTotal.add(weightBD));
            }
        }

        Map<Long, BigDecimal> ingredientTotalGrams = new HashMap<>();

        for (DietDishCookingBranch branch : branches) {
            BigDecimal branchTotalWeight = branchTotalWeights.get(branch.getBranchId());
            if (branchTotalWeight.compareTo(BigDecimal.ZERO) <= 0) continue;

            LambdaQueryWrapper<DietDishIngredient> ingredWrapper = new LambdaQueryWrapper<>();
            ingredWrapper.eq(DietDishIngredient::getBranchId, branch.getBranchId())
                    .eq(DietDishIngredient::getDelFlag, 0);
            List<DietDishIngredient> recipeIngredients = dishIngredientMapper.selectList(ingredWrapper);
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

        for (Map.Entry<Long, BigDecimal> entry : ingredientTotalGrams.entrySet()) {
            DietFamilyMealGrocery grocery = new DietFamilyMealGrocery();
            grocery.setMealPlanId(mealPlanId);
            grocery.setIngredientId(entry.getKey());
            grocery.setUseAmount(entry.getValue().setScale(2, RoundingMode.HALF_UP));
            familyMealGroceryMapper.insert(grocery);
        }

        return mealPlan;
    }

    /**
     * 确认就餐打卡
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeMeal(Long mealPlanId, List<DislikeFeedback> dislikes) {
        DietFamilyMealPlan mealPlan = familyMealPlanMapper.selectById(mealPlanId);
        if (mealPlan == null) return;

        Long groupId = mealPlan.getGroupId();

        LambdaQueryWrapper<DietFamilyMealPlanDish> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(DietFamilyMealPlanDish::getMealPlanId, mealPlanId)
                .eq(DietFamilyMealPlanDish::getDelFlag, 0);
        List<DietFamilyMealPlanDish> relations = familyMealPlanDishMapper.selectList(relWrapper);
        if (relations.isEmpty()) return;

        List<Long> branchIds = relations.stream().map(DietFamilyMealPlanDish::getBranchId).collect(Collectors.toList());
        List<DietDishCookingBranch> branches = dishCookingBranchMapper.selectBatchIds(branchIds);
        List<Long> dishIds = branches.stream().map(DietDishCookingBranch::getDishId).distinct().collect(Collectors.toList());

        LambdaQueryWrapper<DietUserHealthProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(DietUserHealthProfile::getGroupId, groupId)
                .eq(DietUserHealthProfile::getDelFlag, 0);
        List<DietUserHealthProfile> profiles = userHealthProfileMapper.selectList(profileWrapper);

        Long cookUserId = null;
        for (DietUserHealthProfile p : profiles) {
            if (p.getGroupRole() == 1) {
                cookUserId = p.getUserId();
                break;
            }
        }

        if (cookUserId != null) {
            for (Long dishId : dishIds) {
                LambdaQueryWrapper<DietCookDishStat> statCheck = new LambdaQueryWrapper<>();
                statCheck.eq(DietCookDishStat::getUserId, cookUserId)
                        .eq(DietCookDishStat::getDishId, dishId);
                DietCookDishStat stat = cookDishStatMapper.selectOne(statCheck);

                if (stat == null) {
                    stat = new DietCookDishStat();
                    stat.setUserId(cookUserId);
                    stat.setDishId(dishId);
                    stat.setCookCount(1);
                    stat.setSignatureFlag(0);
                    cookDishStatMapper.insert(stat);
                } else {
                    int count = stat.getCookCount() + 1;
                    stat.setCookCount(count);
                    if (count >= 5 && stat.getSignatureFlag() == 0) {
                        stat.setSignatureFlag(1);

                        LambdaQueryWrapper<DietCookSkilledDish> skillCheck = new LambdaQueryWrapper<>();
                        skillCheck.eq(DietCookSkilledDish::getUserId, cookUserId)
                                .eq(DietCookSkilledDish::getDishId, dishId);
                        if (cookSkilledDishMapper.selectCount(skillCheck) == 0) {
                            DietCookSkilledDish skill = new DietCookSkilledDish();
                            skill.setUserId(cookUserId);
                            skill.setDishId(dishId);
                            cookSkilledDishMapper.insert(skill);
                        }
                    }
                    stat.setUpdateTime(LocalDateTime.now());
                    cookDishStatMapper.updateById(stat);
                }
            }
        }

        if (dislikes != null && !dislikes.isEmpty()) {
            for (DislikeFeedback feedback : dislikes) {
                LambdaQueryWrapper<DietUserDislikeDish> dislikeWrapper = new LambdaQueryWrapper<>();
                dislikeWrapper.eq(DietUserDislikeDish::getProfileId, feedback.getProfileId())
                        .eq(DietUserDislikeDish::getDishId, feedback.getDishId());
                DietUserDislikeDish dislike = userDislikeDishMapper.selectOne(dislikeWrapper);

                if (dislike == null) {
                    dislike = new DietUserDislikeDish();
                    dislike.setProfileId(feedback.getProfileId());
                    dislike.setGroupId(groupId);
                    dislike.setDishId(feedback.getDishId());
                    dislike.setDislikeCount(1);
                    userDislikeDishMapper.insert(dislike);
                } else {
                    dislike.setDislikeCount(dislike.getDislikeCount() + 1);
                    dislike.setUpdateTime(LocalDateTime.now());
                    userDislikeDishMapper.updateById(dislike);
                }
            }
        }

        for (DietUserHealthProfile p : profiles) {
            LambdaQueryWrapper<DietUserWishDish> wishDel = new LambdaQueryWrapper<>();
            wishDel.eq(DietUserWishDish::getProfileId, p.getProfileId())
                    .in(DietUserWishDish::getDishId, dishIds);
            userWishDishMapper.delete(wishDel);
        }
    }

    /**
     * 查询某次联合配餐的详情信息 (含配给及食材采购清单)
     */
    public com.diet.modules.biz.model.vo.DietMealDetailVO getMealDetail(Long groupId, LocalDate date, Integer mealPeriod) {
        com.diet.modules.biz.model.vo.DietMealDetailVO vo = new com.diet.modules.biz.model.vo.DietMealDetailVO();

        LambdaQueryWrapper<DietFamilyMealPlan> planQuery = new LambdaQueryWrapper<>();
        planQuery.eq(DietFamilyMealPlan::getGroupId, groupId)
                .eq(DietFamilyMealPlan::getMealDate, date)
                .eq(DietFamilyMealPlan::getMealPeriod, mealPeriod)
                .eq(DietFamilyMealPlan::getDelFlag, 0);
        DietFamilyMealPlan mealPlan = familyMealPlanMapper.selectOne(planQuery);

        if (mealPlan == null) {
            vo.setHasMeal(false);
            return vo;
        }

        vo.setHasMeal(true);
        vo.setMealPlan(mealPlan);
        Long mealPlanId = mealPlan.getMealPlanId();

        // 1. 获取关联的做法分支列表并装填
        LambdaQueryWrapper<DietFamilyMealPlanDish> dishRelQuery = new LambdaQueryWrapper<>();
        dishRelQuery.eq(DietFamilyMealPlanDish::getMealPlanId, mealPlanId)
                .eq(DietFamilyMealPlanDish::getDelFlag, 0);
        List<DietFamilyMealPlanDish> dishRels = familyMealPlanDishMapper.selectList(dishRelQuery);

        List<DietDishCookingBranch> dishes = new ArrayList<>();
        if (!dishRels.isEmpty()) {
            List<Long> branchIds = dishRels.stream().map(DietFamilyMealPlanDish::getBranchId).collect(Collectors.toList());
            List<DietDishCookingBranch> rawBranches = dishCookingBranchMapper.selectBatchIds(branchIds);
            for (DietDishCookingBranch b : rawBranches) {
                DietDish dish = dishMapper.selectById(b.getDishId());
                if (dish != null) {
                    // 对做法分支的名字进行人性化拼接，如：西兰花炒牛肉 (健身无油版)
                    b.setBranchName(dish.getDishName() + " (" + b.getBranchName() + ")");
                }
                dishes.add(b);
            }
        }
        vo.setDishes(dishes);

        // 2. 获取吃饭分配比例
        LambdaQueryWrapper<DietFamilyMealPortion> portionQuery = new LambdaQueryWrapper<>();
        portionQuery.eq(DietFamilyMealPortion::getMealPlanId, mealPlanId)
                .eq(DietFamilyMealPortion::getDelFlag, 0);
        List<DietFamilyMealPortion> portions = familyMealPortionMapper.selectList(portionQuery);

        List<com.diet.modules.biz.model.vo.DietPortionVO> portionDetails = new ArrayList<>();
        for (DietFamilyMealPortion portion : portions) {
            com.diet.modules.biz.model.vo.DietPortionVO item = new com.diet.modules.biz.model.vo.DietPortionVO();
            item.setPortionId(portion.getPortionId());
            item.setDishId(portion.getBranchId()); // VO 的属性仍名 dishId，在此将做法分支关联赋值
            item.setRecommendWeight(portion.getRecommendWeight());
            item.setProfileId(portion.getProfileId());

            DietUserHealthProfile profile = userHealthProfileMapper.selectById(portion.getProfileId());
            if (profile != null) {
                item.setMemberName(profile.getMemberName());
                item.setMemberRelation(profile.getMemberRelation());
            }

            portionDetails.add(item);
        }
        vo.setPortions(portionDetails);

        // 3. 获取采购清单
        LambdaQueryWrapper<DietFamilyMealGrocery> groceryQuery = new LambdaQueryWrapper<>();
        groceryQuery.eq(DietFamilyMealGrocery::getMealPlanId, mealPlanId)
                .eq(DietFamilyMealGrocery::getDelFlag, 0);
        List<DietFamilyMealGrocery> groceries = familyMealGroceryMapper.selectList(groceryQuery);

        List<com.diet.modules.biz.model.vo.DietGroceryVO> groceryDetails = new ArrayList<>();
        for (DietFamilyMealGrocery grocery : groceries) {
            com.diet.modules.biz.model.vo.DietGroceryVO item = new com.diet.modules.biz.model.vo.DietGroceryVO();
            item.setIngredientId(grocery.getIngredientId());
            item.setUseAmount(grocery.getUseAmount());

            DietIngredient ing = ingredientMapper.selectById(grocery.getIngredientId());
            if (ing != null) {
                item.setIngredientName(ing.getIngredientName());
                item.setMeasureUnit(ing.getMeasureUnit());
                item.setCondimentFlag(ing.getCondimentFlag());
            }
            groceryDetails.add(item);
        }
        vo.setGroceries(groceryDetails);

        return vo;
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
                    copy.setCondimentFlag(grocery.getCondimentFlag());
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
        DietFamilyGroup group = familyGroupMapper.selectById(groupId);
        return (group != null) ? group.getCooldownDays() : 7;
    }

    /**
     * 保存避重冷却天数
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveCooldownDays(Long groupId, Integer cooldownDays) {
        DietFamilyGroup group = familyGroupMapper.selectById(groupId);
        if (group != null) {
            group.setCooldownDays(cooldownDays);
            group.setUpdateTime(LocalDateTime.now());
            return familyGroupMapper.updateById(group) > 0;
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
