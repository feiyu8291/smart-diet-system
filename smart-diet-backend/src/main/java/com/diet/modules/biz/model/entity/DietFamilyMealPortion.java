package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("diet_family_meal_portion")
public class DietFamilyMealPortion {
    @TableId(type = IdType.AUTO)
    private Long portionId;
    private Long mealPlanId;
    private Long profileId; // 关联 user_health_profile.profile_id (就餐人档案)
    private Long dishId;
    private BigDecimal recommendWeight;

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
