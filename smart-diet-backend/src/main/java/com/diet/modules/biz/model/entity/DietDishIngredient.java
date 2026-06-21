package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("diet_dish_ingredient")
public class DietDishIngredient {
    @TableId(type = IdType.AUTO)
    private Long relationId;
    private Long dishId;
    private Long ingredientId;
    private BigDecimal useAmount;
    private Integer mainMaterialFlag; // 1-核心主料, 0-辅料调味

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
