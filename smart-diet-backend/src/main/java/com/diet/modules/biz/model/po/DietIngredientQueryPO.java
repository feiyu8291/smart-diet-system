package com.diet.modules.biz.model.po;

import com.diet.modules.common.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 原材料分页查询条件 PO
 *
 * @author FeiYu
 * @date 2026-06-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "原材料分页查询条件实体类")
public class DietIngredientQueryPO extends BasePageQuery {

    @Schema(description = "原材料名称")
    private String name;

    @Schema(description = "食材类型(1-荤菜类, 2-素菜类, 3-调辅配料, 4-基础调味)")
    private Integer ingredientType;
}
