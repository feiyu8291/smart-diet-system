package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜品步骤关联保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Data
@Schema(description = "菜谱烹饪步骤保存传输对象")
public class DietDishStepSaveDTO {
    @Schema(description = "步骤公共池主键ID")
    private Long stepPoolId;
    @Schema(description = "操作步骤序号")
    private Integer stepNum;
    @Schema(description = "该步骤自定义微调描述")
    private String customDetail;
    @Schema(description = "推荐烹饪秒数")
    private Integer durationSeconds;
    @Schema(description = "推荐火候")
    private Integer firePower;
}
