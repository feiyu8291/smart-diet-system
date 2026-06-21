package com.diet.modules.biz.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 食材采购清单视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietGroceryVO {
    private Long ingredientId;
    private BigDecimal useAmount;
    private String ingredientName;
    private String measureUnit;
    private Integer condimentFlag;
}
