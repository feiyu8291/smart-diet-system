package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "成员健康档案详情视图对象")
public class DietUserHealthProfileVO {
    @Schema(description = "档案ID")
    private Long profileId;
    @Schema(description = "做饭人或成员系统用户ID (关联sys_user.user_id)")
    private Long userId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "成员角色 (1-做饭人, 2-普通成员)")
    private Integer groupRole;
    @Schema(description = "成员姓名")
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
    @Schema(description = "成员年龄")
    private Integer memberAge;
    @Schema(description = "活动强度等级 (1-久坐, 2-轻度, 3-中度, 4-重度)")
    private Integer activityLevel;
    @Schema(description = "目标体重 (kg)")
    private BigDecimal targetWeight;
    @Schema(description = "减重速度 (kg/周)")
    private BigDecimal dietSpeed;
    @Schema(description = "BMR 基础代谢 (kcal)")
    private BigDecimal bmrCalories;
    @Schema(description = "TDEE 每日总消耗 (kcal)")
    private BigDecimal tdeeCalories;
    @Schema(description = "每日推荐摄入目标 (kcal)")
    private BigDecimal dailyTargetCalories;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "家庭组名称")
    private String groupName;
}
