package com.diet.modules.biz.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 成员健康档案保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietUserHealthProfileDTO {
    private Long profileId;
    private Long userId;
    private Long groupId;
    private Integer groupRole;
    private String memberName;
    private String memberRelation;
    private Integer memberGender;
    private BigDecimal memberHeight;
    private BigDecimal memberWeight;
    private Integer memberAge;
    private Integer activityLevel;
    private BigDecimal targetWeight;
    private BigDecimal dietSpeed;
}
