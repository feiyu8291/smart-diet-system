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
@TableName("diet_cooking_step_pool")
@Schema(description = "公共烹饪步骤池实体类")
public class DietCookingStepPool extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "步骤公共池主键ID")
    private Long stepPoolId;
    @Schema(description = "操作步骤名称")
    private String stepName;
    @Schema(description = "操作步骤详情")
    private String stepDetail;

}
