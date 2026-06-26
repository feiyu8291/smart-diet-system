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
@TableName("diet_dish_step_relation")
@Schema(description = "菜谱与烹饪步骤关联实体类")
public class DietDishStepRelation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "关联主键ID")
    private Long relationId;
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "做法分支ID")
    private Long branchId;
    @Schema(description = "步骤公共池主键ID")
    private Long stepPoolId;
    @Schema(description = "stepNum")
    private Integer stepNum;
    @Schema(description = "customDetail")
    private String customDetail;
    @Schema(description = "推荐烹饪秒数")
    private Integer durationSeconds;
    @Schema(description = "推荐火候")
    private Integer firePower;

}
