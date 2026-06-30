package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 烹饪标准步骤模板池视图 VO
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Data
@Schema(description = "烹饪步骤模板视图对象")
public class DietCookingStepPoolVO {
    @Schema(description = "步骤公共池主键ID")
    private Long stepPoolId;
    @Schema(description = "操作步骤名称")
    private String stepName;
    @Schema(description = "操作步骤详情")
    private String stepDetail;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
