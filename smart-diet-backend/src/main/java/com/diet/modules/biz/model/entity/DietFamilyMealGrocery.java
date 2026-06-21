package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("diet_family_meal_grocery")
public class DietFamilyMealGrocery {
    @TableId(type = IdType.AUTO)
    private Long groceryId;
    private Long mealPlanId;
    private Long ingredientId;
    private BigDecimal useAmount;

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
