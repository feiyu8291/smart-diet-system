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
@TableName("diet_family_meal_grocery")
@Schema(description = "家庭配餐采购清单实体类")
public class DietFamilyMealGrocery extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "采购物品ID")
    private Long groceryId;
    @Schema(description = "mealPlanId")
    private Long mealPlanId;
    @Schema(description = "原材料ID")
    private Long ingredientId;
    @Schema(description = "useAmount")
    private BigDecimal useAmount;

}
