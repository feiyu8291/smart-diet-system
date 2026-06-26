package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_dish_ingredient")
@Schema(description = "菜谱原材料关联实体类")
public class DietDishIngredient extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联主键ID")
    private Long relationId;
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "做法分支ID")
    private Long branchId;
    @Schema(description = "原材料ID")
    private Long ingredientId;
    @Schema(description = "useAmount")
    private BigDecimal useAmount;
    @Schema(description = "1-核心主料, 0-辅料调味")
    private Integer mainMaterialFlag;

}
