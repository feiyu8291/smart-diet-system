package com.diet.modules.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.biz.model.entity.DietPlan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DietPlanMapper extends BaseMapper<DietPlan> {
}
