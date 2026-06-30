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
    @Schema(description = "档案ID")
    private Long profileId;
    @Schema(description = "做饭人或成员系统用户ID (关联sys_user.user_id)")
    private Long userId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "成员角色 (1-做饭人, 2-普通成员)")
    private Integer groupRole;
    @Schema(description = "就餐人姓名")
    private String memberName;
    @Schema(description = "与做饭人关系")
    private String memberRelation;
    @Schema(description = "成员性别 (1-男, 2-女)")
    private Integer memberGender;
    @Schema(description = "成员身高 (cm)")
    private BigDecimal memberHeight;
    @Schema(description = "当前体重 (kg)")
    private BigDecimal memberWeight;
    @Schema(description = "出生日期")
    private java.time.LocalDate memberBirthday;
    @Schema(description = "活动强度等级 (1-久坐, 2-轻度, 3-中度, 4-重度)")
    private Integer activityLevel;
    @Schema(description = "目标体重 (kg)")
    private BigDecimal targetWeight;
    @Schema(description = "减重速度 (kg/周)")
    private BigDecimal dietSpeed;
}
