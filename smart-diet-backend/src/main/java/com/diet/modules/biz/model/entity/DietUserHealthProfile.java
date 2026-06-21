package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("diet_user_health_profile")
public class DietUserHealthProfile {
    @TableId(type = IdType.AUTO)
    private Long profileId;
    private Long userId; // 系统注册用户 user_id (在线为关联id，离线为NULL)
    private Long groupId;
    private Integer groupRole; // 1-做饭人, 2-普通成员
    private String memberName;
    private String memberRelation;
    private Integer memberGender; // 1-男, 2-女
    private BigDecimal memberHeight;
    private BigDecimal memberWeight;
    private Integer memberAge;
    private Integer activityLevel; // 1-久坐, 2-轻度, 3-中度, 4-重度
    private BigDecimal targetWeight;
    private BigDecimal dietSpeed; // 减重速度 (kg/周)
    private BigDecimal bmrCalories;
    private BigDecimal tdeeCalories; // 每日总消耗 kcal
    private BigDecimal dailyTargetCalories;

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
