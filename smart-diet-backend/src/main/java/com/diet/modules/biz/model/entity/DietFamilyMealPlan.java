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
@TableName("diet_family_meal_plan")
@Schema(description = "家庭配餐计划实体类")
public class DietFamilyMealPlan extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "mealPlanId")
    private Long mealPlanId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "mealDate")
    private LocalDate mealDate;
    @Schema(description = "1-早餐, 2-午餐, 3-晚餐")
    private Integer mealPeriod;
    @Schema(description = "0-正常, 1-轻食, 2-放纵")
    private Integer mealDietMode;

}
