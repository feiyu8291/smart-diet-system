package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("diet_dish")
public class DietDish {
    @TableId(type = IdType.AUTO)
    private Long dishId;
    private String dishName;
    private String cuisineType;
    private Integer dietMode; // 0-正常饮食, 1-轻食减脂, 2-放纵餐
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private Long coverImageId;
    private String imageIds;

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
