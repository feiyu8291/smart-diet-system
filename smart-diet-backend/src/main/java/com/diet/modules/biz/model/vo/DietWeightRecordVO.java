package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体重历史记录视图 VO
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Data
@Schema(description = "体重历史记录视图对象")
public class DietWeightRecordVO {
    @Schema(description = "记录ID")
    private Long recordId;
    @Schema(description = "对应成员档案ID")
    private Long profileId;
    @Schema(description = "测定体重 (kg)")
    private BigDecimal recordWeight;
    @Schema(description = "记录日期")
    private LocalDate recordDate;
    @Schema(description = "关联执行计划进度ID")
    private Long planProgressId;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
