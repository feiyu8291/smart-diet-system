package com.diet.modules.quartz.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务修改PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "定时任务修改PO")
public class SysQuartzJobUpdatePO extends SysQuartzJobSavePO {

    @NotNull(message = "任务id不能为空")
    @Schema(description = "主键ID")
    private Long id;
}
