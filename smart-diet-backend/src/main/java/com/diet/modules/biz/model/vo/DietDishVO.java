package com.diet.modules.biz.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品摘要视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietDishVO {
    private Long dishId;
    private String dishName;
    private String cuisineType;
    private String dietMode;
    private Integer calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private String previewUrl;
    private Integer cookCount = 0;
    private Integer signatureFlag = 0;
    private Integer isSkilled = 0;
}
