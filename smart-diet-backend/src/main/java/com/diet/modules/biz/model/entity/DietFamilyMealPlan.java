package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("diet_family_meal_plan")
public class DietFamilyMealPlan {
    @TableId(type = IdType.AUTO)
    private Long mealPlanId;
    private Long groupId;
    private LocalDate mealDate;
    private Integer mealPeriod; // 1-早餐, 2-午餐, 3-晚餐
    private Integer mealDietMode; // 0-正常, 1-轻食, 2-放纵

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
