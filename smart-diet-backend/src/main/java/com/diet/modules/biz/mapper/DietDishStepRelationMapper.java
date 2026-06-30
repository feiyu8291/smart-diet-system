package com.diet.modules.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.biz.model.entity.DietDishStepRelation;
import com.diet.modules.biz.model.vo.DietDishStepVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DietDishStepRelationMapper extends BaseMapper<DietDishStepRelation> {

    /**
     * 根据做法分支ID多表关联查询烹饪步骤详情
     *
     * @param branchId 做法分支ID
     * @return 做法步骤列表
     */
    List<DietDishStepVO> selectRecipeStepsByBranchId(@Param("branchId") Long branchId);
}

