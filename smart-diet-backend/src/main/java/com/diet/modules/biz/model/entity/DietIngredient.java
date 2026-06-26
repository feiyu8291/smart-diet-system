package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.aspect.DictAnnotation;
import com.diet.modules.common.aspect.DictTypeEnum;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_ingredient")
@Schema(description = "系统食材原材料基本信息实体类")
public class DietIngredient extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "原材料ID")
    private Long ingredientId;
    @Schema(description = "原材料名称")
    private String ingredientName;
    @Schema(description = "卡路里 (kcal)")
    private BigDecimal calories;
    @Schema(description = "蛋白质 (g)")
    private BigDecimal protein;
    @Schema(description = "脂肪 (g)")
    private BigDecimal fat;
    @Schema(description = "碳水化合物 (g)")
    private BigDecimal carbs;
    @Schema(description = "measureUnit")
    private String measureUnit;
    @Schema(description = "食材类型(1-荤菜类, 2-素菜类, 3-调辅配料, 4-基础调味)")
    @DictAnnotation(type = DictTypeEnum.INGREDIENT_TYPE, target = "ingredientTypeLabel")
    private Integer ingredientType;
    @TableField(exist = false)
    @Schema(description = "食材类型中文名称")
    private String ingredientTypeLabel;
    @Schema(description = "食材描述(健康选购及烹饪备注)")
    private String ingredientDesc;

}
