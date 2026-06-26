package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 成员健康档案保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "成员健康档案保存传输对象")
public class DietUserHealthProfileDTO {
    @Schema(description = "profileId")
    private Long profileId;
    @Schema(description = "做饭人或成员系统用户ID (关联sys_user.user_id)")
    private Long userId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "groupRole")
    private Integer groupRole;
    @Schema(description = "memberName")
    private String memberName;
    @Schema(description = "memberRelation")
    private String memberRelation;
    @Schema(description = "memberGender")
    private Integer memberGender;
    @Schema(description = "memberHeight")
    private BigDecimal memberHeight;
    @Schema(description = "memberWeight")
    private BigDecimal memberWeight;
    @Schema(description = "memberBirthday")
    private java.time.LocalDate memberBirthday;
    @Schema(description = "活动强度等级 (1-久坐, 2-轻度, 3-中度, 4-重度)")
    private Integer activityLevel;
    @Schema(description = "目标体重 (kg)")
    private BigDecimal targetWeight;
    @Schema(description = "dietSpeed")
    private BigDecimal dietSpeed;
}
