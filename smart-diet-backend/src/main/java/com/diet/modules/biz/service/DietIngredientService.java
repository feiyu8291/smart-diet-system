package com.diet.modules.biz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietIngredientMapper;
import com.diet.modules.biz.model.entity.DietIngredient;
import com.diet.modules.common.aspect.DictData;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 原材料/食材配料业务服务类
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Service
public class DietIngredientService extends ServiceImpl<DietIngredientMapper, DietIngredient> {

    /**
     * 查询原材料列表 (带字典自动翻译)
     */
    @DictData
    public List<DietIngredient> listIngredients(LambdaQueryWrapper<DietIngredient> query) {
        return this.list(query);
    }

    /**
     * 原材料分页查询 (带字典自动翻译)
     */
    @DictData
    public IPage<DietIngredient> pageIngredients(IPage<DietIngredient> page, LambdaQueryWrapper<DietIngredient> query) {
        return this.page(page, query);
    }
}
