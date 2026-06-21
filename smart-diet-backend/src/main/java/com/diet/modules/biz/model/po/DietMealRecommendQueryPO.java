package com.diet.modules.biz.model.po;

import lombok.Data;

/**
 * 联合配餐推荐查询 PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietMealRecommendQueryPO {
    private Long groupId;
    private String targetDate;
    private Integer mealPeriod;
    private Integer dietMode;
    private Integer limit = 2;
}
