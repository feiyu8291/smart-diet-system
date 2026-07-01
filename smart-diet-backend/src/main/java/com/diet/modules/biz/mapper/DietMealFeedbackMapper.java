package com.diet.modules.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diet.modules.biz.model.entity.DietMealFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用餐就餐打卡反馈表 Mapper 映射接口
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Mapper
public interface DietMealFeedbackMapper extends BaseMapper<DietMealFeedback> {
}
