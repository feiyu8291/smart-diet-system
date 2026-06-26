package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 吃饭配给比例视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "成员就餐份量比例详情视图对象")
public class DietPortionVO {
    @Schema(description = "份量比例ID")
    private Long portionId;
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "recommendWeight")
    private BigDecimal recommendWeight;
    @Schema(description = "profileId")
    private Long profileId;
    @Schema(description = "memberName")
    private String memberName;
    @Schema(description = "memberRelation")
    private String memberRelation;
}
