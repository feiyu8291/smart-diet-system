package com.diet.modules.biz.model.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 成员就餐膳食分量查询请求参数持久化/持久传输对象
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Data
@Schema(description = "就餐分量查询请求PO")
public class DietMemberMealPortionQueryPO {
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "就餐人健康档案ID")
    private Long profileId;
    @Schema(description = "目标就餐日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
