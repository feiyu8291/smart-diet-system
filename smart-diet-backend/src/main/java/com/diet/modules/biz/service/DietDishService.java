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
}
