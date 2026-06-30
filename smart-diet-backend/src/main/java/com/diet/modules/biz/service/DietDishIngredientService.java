package com.diet.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietDishIngredientMapper;
import com.diet.modules.biz.model.entity.DietDishIngredient;
import org.springframework.stereotype.Service;

/**
 * 菜谱原料关联业务服务类
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Service
public class DietDishIngredientService extends ServiceImpl<DietDishIngredientMapper, DietDishIngredient> {
}
