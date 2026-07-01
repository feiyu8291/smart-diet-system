package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "单餐中具体菜品分配给个人的膳食分量VO")
public class DietMemberDishPortionVO {
    @Schema(description = "份量分配ID")
    private Long id;
    @Schema(description = "菜品做法名称")
    private String name;
    @Schema(description = "分配进食量克数描述，如：150 克")
    private String portion;
    @Schema(description = "计算分配后的热量(kcal)")
    private BigDecimal calories;
    @Schema(description = "计算分配后的蛋白质(g)")
    private BigDecimal protein;
    @Schema(description = "计算分配后的脂肪(g)")
    private BigDecimal fat;
    @Schema(description = "计算分配后的碳水化合物(g)")
    private BigDecimal carbs;
    @Schema(description = "备注说明")
    private String note;
}
