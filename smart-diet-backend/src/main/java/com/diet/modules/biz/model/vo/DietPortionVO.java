package com.diet.modules.biz.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 吃饭配给比例视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietPortionVO {
    private Long portionId;
    private Long dishId;
    private BigDecimal recommendWeight;
    private Long profileId;
    private String memberName;
    private String memberRelation;
}
