package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("diet_weight_record")
public class DietWeightRecord {
    @TableId(type = IdType.AUTO)
    private Long recordId;
    private Long profileId; // 对应 user_health_profile.profile_id
    private BigDecimal recordWeight;
    private LocalDate recordDate;
    private Long planProgressId; // 关联执行计划进度ID (0为日常测重)

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
