package com.diet.modules.biz.model.po;

import com.diet.modules.common.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 烹饪步骤分页查询条件 PO
 *
 * @author FeiYu
 * @date 2026-06-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "烹饪步骤分页查询条件实体类")
public class DietCookingStepQueryPO extends BasePageQuery {

    @Schema(description = "步骤名称")
    private String name;
}
