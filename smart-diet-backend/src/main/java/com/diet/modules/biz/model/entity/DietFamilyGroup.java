package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("diet_family_group")
public class DietFamilyGroup {
    @TableId(type = IdType.AUTO)
    private Long groupId;
    private String groupName;
    private Long creatorUserId;
    private Integer cooldownDays;

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
