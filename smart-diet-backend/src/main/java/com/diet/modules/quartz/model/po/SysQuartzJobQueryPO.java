package com.diet.modules.quartz.model.po;

import com.diet.modules.common.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务分页查询
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "定时任务查询PO")
public class SysQuartzJobQueryPO extends BasePageQuery {

    @Schema(description = "任务名 (模糊)")
    private String jobName;

    @Schema(description = "任务类型 1系统 2业务")
    private Integer jobType;

    @Schema(description = "是否停用 0否 1是")
    private Integer delFlag;
}
