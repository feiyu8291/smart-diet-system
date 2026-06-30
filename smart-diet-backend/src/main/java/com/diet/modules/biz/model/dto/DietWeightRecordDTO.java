package com.diet.modules.biz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 登记成员体重 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "体重记录保存传输对象")
public class DietWeightRecordDTO {
    @Schema(description = "就餐成员档案ID")
    private Long profileId;
    @Schema(description = "测定体重 (kg)")
    private BigDecimal recordWeight;
}
