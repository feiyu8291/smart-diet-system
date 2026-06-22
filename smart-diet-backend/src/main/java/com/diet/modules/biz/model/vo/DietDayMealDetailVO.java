package com.diet.modules.biz.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 一整天包含三餐与合并采购清单的数据传输视图对象
 *
 * @author FeiYu
 * @date 2026-06-21
 */
@Data
public class DietDayMealDetailVO {
    /**
     * 本日是否有配餐
     */
    private Boolean hasMeal;

    /**
     * 早餐详情
     */
    private DietMealDetailVO breakfast;

    /**
     * 午餐详情
     */
    private DietMealDetailVO lunch;

    /**
     * 晚餐详情
     */
    private DietMealDetailVO dinner;

    /**
     * 合并后的全天食材采购总清单
     */
    private List<DietGroceryVO> dailyGroceries;
}
