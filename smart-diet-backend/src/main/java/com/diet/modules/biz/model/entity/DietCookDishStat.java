package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("diet_cook_dish_stat")
public class DietCookDishStat {
    @TableId(type = IdType.AUTO)
    private Long statId;
    private Long userId; // 做饭人系统用户ID (关联sys_user.user_id)
    private Long dishId;
    private Integer cookCount;
    private Integer signatureFlag; // 拿手菜标志 (0-非拿手菜, 1-该做饭人的拿手菜)

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
