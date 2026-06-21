package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("diet_user_dislike_dish")
public class DietUserDislikeDish {
    @TableId(type = IdType.AUTO)
    private Long dislikeId;
    private Long profileId; // 关联 user_health_profile.profile_id (就餐人档案)
    private Long groupId;
    private Long dishId;
    private Integer dislikeCount;

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
