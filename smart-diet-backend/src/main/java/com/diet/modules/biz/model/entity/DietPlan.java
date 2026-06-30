package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_plan")
@Schema(description = "配餐计划基本信息实体类")
public class DietPlan extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "计划ID")
    private Long planId;
    @Schema(description = "计划名称")
    private String planName;
    @Schema(description = "总天数")
    private Integer totalDays;
    @Schema(description = "计划具体阶段指引描述")
    private String planDescription;

}
