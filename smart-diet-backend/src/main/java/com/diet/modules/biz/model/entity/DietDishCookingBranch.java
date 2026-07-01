package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.biz.model.vo.DietDishStepVO;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜谱烹饪做法分支实体类
 *
 * @author FeiYu
 * @date 2026-06-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_dish_cooking_branch")
@Schema(description = "菜谱烹饪做法分支实体类")
public class DietDishCookingBranch extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键做法分支ID")
    private Long branchId;

    @Schema(description = "关联主菜品ID")
    private Long dishId;

    @Schema(description = "做法分支名称(如：无糖少油版)")
    private String branchName;

    @Schema(description = "做法专属菜系类型")
    private String cuisineType;

    @Schema(description = "建议就餐膳食模式(0-正常饮食, 1-轻食减脂, 2-放纵餐)")
    private Integer dietMode;

    @Schema(description = "做法提交创作者ID")
    private Long creatorUserId;

    @Schema(description = "做法分支封面图片ID(关联sys_file_storage.storage_id)")
    private Long coverImageId;

    @Schema(description = "每100g合算热量(kcal)")
    private BigDecimal calories;

    @Schema(description = "每100g合算蛋白质(g)")
    private BigDecimal protein;

    @Schema(description = "每100g合算脂肪(g)")
    private BigDecimal fat;

    @Schema(description = "每100g合算碳水(g)")
    private BigDecimal carbs;

    @TableField(exist = false)
    @Schema(description = "是否烹饪完成(0否1是)")
    private Integer cookFlag;

    @TableField(exist = false)
    @Schema(description = "烹饪步骤列表")
    private List<DietDishStepVO> steps;

}
