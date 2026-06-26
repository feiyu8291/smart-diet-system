package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_family_plan_progress")
@Schema(description = "家庭配餐计划进度实体类")
public class DietFamilyPlanProgress extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "进度ID")
    private Long progressId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "计划ID")
    private Long planId;
    @Schema(description = "计划开始日期")
    private LocalDate startDate;
    @Schema(description = "当前第几天")
    private Integer currentDay;
    @Schema(description = "1-进行中, 2-已完成, 3-已中断")
    private Integer progressStatus;

}
