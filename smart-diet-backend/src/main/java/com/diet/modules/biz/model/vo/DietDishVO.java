package com.diet.modules.biz.model.vo;

import com.diet.modules.common.aspect.DictAnnotation;
import com.diet.modules.common.aspect.DictTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜品摘要视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "菜谱分页列表展示视图对象")
public class DietDishVO {

    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "做法分支ID")
    private Long branchId;
    @Schema(description = "做法分支名称")
    private String branchName;
    @Schema(description = "菜谱名称")
    private String dishName;
    @Schema(description = "菜系类型")
    @DictAnnotation(type = DictTypeEnum.CUISINE_TYPE)
    private String cuisineType;
    private String cuisineTypeStr;
    @Schema(description = "建议就餐模式 (0-正常饮食, 1-轻食减脂, 2-放纵餐)")
    @DictAnnotation(type = DictTypeEnum.DIET_MODE_TYPE)
    private String dietMode;
    private String dietModeStr;
    @Schema(description = "卡路里 (kcal)")
    private Integer calories;
    @Schema(description = "蛋白质 (g)")
    private BigDecimal protein;
    @Schema(description = "脂肪 (g)")
    private BigDecimal fat;
    @Schema(description = "碳水化合物 (g)")
    private BigDecimal carbs;
    @Schema(description = "菜品成品图预览地址")
    private String previewUrl;
    private Integer cookCount = 0;
    private Integer signatureFlag = 0;
    private Integer isSkilled = 0;
    @Schema(description = "做法分支列表")
    private List<DietDishBranchVO> branches;
}
