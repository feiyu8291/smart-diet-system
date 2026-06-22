package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.*;
import com.diet.modules.biz.model.dto.DietDislikeDTO;
import com.diet.modules.biz.model.dto.DietSkilledDTO;
import com.diet.modules.biz.model.dto.DietWishDTO;
import com.diet.modules.biz.model.entity.*;
import com.diet.modules.biz.model.po.DietDishQueryPO;
import com.diet.modules.biz.model.vo.DietDishDetailVO;
import com.diet.modules.biz.model.vo.DietDishIngredientVO;
import com.diet.modules.biz.model.vo.DietDishStepVO;
import com.diet.modules.biz.model.vo.DietDishVO;
import com.diet.modules.system.mapper.SysFileStorageMapper;
import com.diet.modules.system.model.entity.SysFileStorage;
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
     * 获取指定菜谱的详情信息 (配料原料列表和做法步骤列表)
     */
    public DietDishDetailVO getDishDetail(Long dishId) {
        DietDish dish = dishMapper.selectById(dishId);
        if (dish == null || dish.getDelFlag() == 1) {
            throw new RuntimeException("菜品不存在");
        }

        DietDishDetailVO detailVO = new DietDishDetailVO();
        detailVO.setDish(dish);

        // 1. 获取配方原料列表 (dish_ingredient)
        LambdaQueryWrapper<DietDishIngredient> ingredQuery = new LambdaQueryWrapper<>();
        ingredQuery.eq(DietDishIngredient::getDishId, dishId).eq(DietDishIngredient::getDelFlag, 0);
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
        detailVO.setIngredients(ingredients);

        // 2. 获取做法步骤列表 (dish_step_relation)
        LambdaQueryWrapper<DietDishStepRelation> stepQuery = new LambdaQueryWrapper<>();
        stepQuery.eq(DietDishStepRelation::getDishId, dishId)
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
            steps.add(item);
        }
        detailVO.setSteps(steps);

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
        dish.setCuisineType(dto.getCuisineType());
        dish.setDietMode(dto.getDietMode() != null ? dto.getDietMode() : 0);
        dish.setImageIds(dto.getImageIds());

        // 提取 imageIds 中的第一张图片 ID 作为 coverImageId，以向下兼容
        if (dto.getImageIds() != null && !dto.getImageIds().trim().isEmpty()) {
            String[] ids = dto.getImageIds().split(",");
            if (ids.length > 0 && !ids[0].trim().isEmpty()) {
                try {
                    dish.setCoverImageId(Long.parseLong(ids[0].trim()));
                } catch (NumberFormatException e) {
                    dish.setCoverImageId(dto.getCoverImageId());
                }
            } else {
                dish.setCoverImageId(dto.getCoverImageId());
            }
        } else {
            dish.setCoverImageId(dto.getCoverImageId());
        }

        dish.setUpdateTime(LocalDateTime.now());

        // 2. 智能核算热量与三大营养素
        // 如果传入了大于0的具体值，优先使用传入值；否则从配料中重算每100克成品菜的营养素
        boolean needsCalc = (dto.getCalories() == null || dto.getCalories().doubleValue() <= 0);

        if (!needsCalc) {
            dish.setCalories(dto.getCalories());
            dish.setProtein(dto.getProtein() != null ? dto.getProtein() : BigDecimal.ZERO);
            dish.setFat(dto.getFat() != null ? dto.getFat() : BigDecimal.ZERO);
            dish.setCarbs(dto.getCarbs() != null ? dto.getCarbs() : BigDecimal.ZERO);
        } else {
            // 根据配料表进行计算
            if (dto.getIngredients() == null || dto.getIngredients().isEmpty()) {
                dish.setCalories(BigDecimal.ZERO);
                dish.setProtein(BigDecimal.ZERO);
                dish.setFat(BigDecimal.ZERO);
                dish.setCarbs(BigDecimal.ZERO);
            } else {
                double totalWeight = 0;
                double totalCals = 0;
                double totalProt = 0;
                double totalFat = 0;
                double totalCarbs = 0;

                for (com.diet.modules.biz.model.dto.DietDishIngredientSaveDTO ingDto : dto.getIngredients()) {
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
                    // 折算为每 100 克成品的营养素
                    dish.setCalories(BigDecimal.valueOf((totalCals / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
                    dish.setProtein(BigDecimal.valueOf((totalProt / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
                    dish.setFat(BigDecimal.valueOf((totalFat / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
                    dish.setCarbs(BigDecimal.valueOf((totalCarbs / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
                } else {
                    dish.setCalories(BigDecimal.ZERO);
                    dish.setProtein(BigDecimal.ZERO);
                    dish.setFat(BigDecimal.ZERO);
                    dish.setCarbs(BigDecimal.ZERO);
                }
            }
        }

        // 保存或修改主表
        if (dto.getDishId() != null) {
            dishMapper.updateById(dish);
        } else {
            dishMapper.insert(dish);
        }
        Long dishId = dish.getDishId();

        // 3. 清理已有的配料与步骤关联
        LambdaQueryWrapper<DietDishIngredient> ingDelete = new LambdaQueryWrapper<>();
        ingDelete.eq(DietDishIngredient::getDishId, dishId);
        dishIngredientMapper.delete(ingDelete);

        LambdaQueryWrapper<DietDishStepRelation> stepDelete = new LambdaQueryWrapper<>();
        stepDelete.eq(DietDishStepRelation::getDishId, dishId);
        dishStepRelationMapper.delete(stepDelete);

        // 4. 插入全新的配料关系
        if (dto.getIngredients() != null) {
            for (com.diet.modules.biz.model.dto.DietDishIngredientSaveDTO ingDto : dto.getIngredients()) {
                DietDishIngredient rel = new DietDishIngredient();
                rel.setDishId(dishId);
                rel.setIngredientId(ingDto.getIngredientId());
                rel.setUseAmount(ingDto.getUseAmount());
                rel.setMainMaterialFlag(ingDto.getMainMaterialFlag() != null ? ingDto.getMainMaterialFlag() : 1);
                rel.setDelFlag(0);
                rel.setCreateTime(LocalDateTime.now());
                rel.setUpdateTime(LocalDateTime.now());
                dishIngredientMapper.insert(rel);
            }
        }

        // 5. 插入全新的步骤关系
        if (dto.getSteps() != null) {
            for (com.diet.modules.biz.model.dto.DietDishStepSaveDTO stepDto : dto.getSteps()) {
                DietDishStepRelation rel = new DietDishStepRelation();
                rel.setDishId(dishId);
                rel.setStepPoolId(stepDto.getStepPoolId() != null ? stepDto.getStepPoolId() : 0L);
                rel.setStepNum(stepDto.getStepNum());
                rel.setCustomDetail(stepDto.getCustomDetail());
                rel.setDelFlag(0);
                rel.setCreateTime(LocalDateTime.now());
                rel.setUpdateTime(LocalDateTime.now());
                dishStepRelationMapper.insert(rel);
            }
        }

        return true;
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
                // 同步软删除其食材与步骤配方关联
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
