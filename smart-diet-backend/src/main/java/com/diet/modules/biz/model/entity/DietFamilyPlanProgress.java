package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("diet_family_plan_progress")
public class DietFamilyPlanProgress {
    @TableId(type = IdType.AUTO)
    private Long progressId;
    private Long groupId;
    private Long planId;
    private LocalDate startDate;
    private Integer currentDay;
    private Integer progressStatus; // 1-进行中, 2-已完成, 3-已中断

    // 审计字段
    private Integer delFlag;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
