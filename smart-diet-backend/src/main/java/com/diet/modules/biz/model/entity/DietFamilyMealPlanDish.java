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
@TableName("diet_family_meal_plan_dish")
@Schema(description = "家庭配餐计划菜谱关联实体类")
public class DietFamilyMealPlanDish extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联主键ID")
    private Long relationId;
    @Schema(description = "排餐计划ID")
    private Long mealPlanId;
    @Schema(description = "做法分支ID")
    private Long branchId;

}
