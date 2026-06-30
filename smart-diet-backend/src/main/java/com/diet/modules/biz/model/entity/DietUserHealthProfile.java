package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_user_health_profile")
@Schema(description = "成员健康档案基本信息实体类")
public class DietUserHealthProfile extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "档案ID")
    private Long profileId;
    @Schema(description = "做饭人或成员系统用户ID (关联sys_user.user_id)")
    private Long userId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "1-做饭人, 2-普通成员")
    private Integer groupRole;
    @Schema(description = "成员姓名")
    private String memberName;
    @Schema(description = "与做饭人关系")
    private String memberRelation;
    @Schema(description = "1-男, 2-女")
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
    @Schema(description = "BMR 基础代谢 (kcal)")
    private BigDecimal bmrCalories;
    @Schema(description = "每日总消耗 kcal")
    private BigDecimal tdeeCalories;
    @Schema(description = "每日推荐摄入目标 (kcal)")
    private BigDecimal dailyTargetCalories;

}
