package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 业务-用餐就餐打卡反馈表实体类
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Data
@TableName("diet_meal_feedback")
@Schema(description = "用餐就餐打卡反馈实体类")
public class DietMealFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long feedbackId;

    @Schema(description = "关联食谱膳食计划ID")
    private Long mealPlanId;

    @Schema(description = "就餐人档案ID")
    private Long profileId;

    @Schema(description = "用餐状态(1-已餐, 2-未餐)")
    private Integer eatStatus;

    @Schema(description = "未餐原因(外出/加班/生病/没有胃口等)")
    private String skipReason;

    @Schema(description = "未餐详细备注")
    private String skipNote;
}
