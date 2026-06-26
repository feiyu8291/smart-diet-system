package com.diet.modules.biz.model.vo;

import com.diet.modules.common.aspect.DictAnnotation;
import com.diet.modules.common.aspect.DictTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜品步骤视图 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "菜谱烹饪步骤详情视图对象")
public class DietDishStepVO {
    @Schema(description = "stepNum")
    private Integer stepNum;
    @Schema(description = "操作步骤详情")
    private String stepDetail;
    @Schema(description = "推荐烹饪秒数")
    private Integer durationSeconds;
    @Schema(description = "推荐火候")
    @DictAnnotation(type = DictTypeEnum.FIRE_POWER_TYPE, target = "firePowerLabel")
    private Integer firePower;
    @Schema(description = "推荐火候中文标签")
    private String firePowerLabel;
}
