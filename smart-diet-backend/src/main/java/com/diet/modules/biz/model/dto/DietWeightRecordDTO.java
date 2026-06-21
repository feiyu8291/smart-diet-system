package com.diet.modules.biz.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 登记成员体重 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietWeightRecordDTO {
    private Long profileId;
    private BigDecimal recordWeight;
}
