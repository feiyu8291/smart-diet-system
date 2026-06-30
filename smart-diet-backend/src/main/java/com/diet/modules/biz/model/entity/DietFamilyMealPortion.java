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
@TableName("diet_family_meal_portion")
@Schema(description = "家庭成员就餐份量比例实体类")
public class DietFamilyMealPortion extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "份量比例ID")
    private Long portionId;
    @Schema(description = "排餐计划ID")
    private Long mealPlanId;
    @Schema(description = "关联 user_health_profile.profile_id (就餐人档案)")
    private Long profileId;
    @Schema(description = "做法分支ID")
    private Long branchId;
    @Schema(description = "计算得出的建议分量 (g)")
    private BigDecimal recommendWeight;

}
