package com.diet.modules.biz.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成员健康档案视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietUserHealthProfileVO {
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
    private BigDecimal bmrCalories;
    private BigDecimal tdeeCalories;
    private BigDecimal dailyTargetCalories;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
