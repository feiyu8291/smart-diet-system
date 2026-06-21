package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("diet_dish_step_relation")
public class DietDishStepRelation {
    @TableId(type = IdType.AUTO)
    private Long relationId;
    private Long dishId;
    private Long stepPoolId;
    private Integer stepNum;
    private String customDetail;

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
