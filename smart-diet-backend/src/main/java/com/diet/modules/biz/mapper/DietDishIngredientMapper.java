package com.diet.modules.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.biz.model.entity.DietDishIngredient;
import com.diet.modules.biz.model.vo.DietDishIngredientVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DietDishIngredientMapper extends BaseMapper<DietDishIngredient> {

    /**
     * 根据做法分支ID多表关联查询配料详情
     *
     * @param branchId 做法分支ID
     * @return 配方原料列表
     */
    List<DietDishIngredientVO> selectRecipeIngredientsByBranchId(@Param("branchId") Long branchId);
}

