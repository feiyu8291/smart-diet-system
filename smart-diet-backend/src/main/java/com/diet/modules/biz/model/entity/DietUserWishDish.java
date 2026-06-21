package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("diet_user_wish_dish")
public class DietUserWishDish {
    @TableId(type = IdType.AUTO)
    private Long wishId;
    private Long profileId; // 关联 user_health_profile.profile_id (就餐人档案)
    private Long groupId;
    private Long dishId;
    private LocalDate wishDate; // 希望就餐日期，可为 NULL

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
