package com.diet.modules.biz.model.po;

import lombok.Data;

/**
 * 联合配餐详情查询 PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietMealDetailQueryPO {
    private Long groupId;
    private String targetDate;
    private Integer mealPeriod;
}
