package com.diet.modules.biz.model.po;

import com.diet.modules.common.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 成员健康档案分页查询条件 PO
 *
 * @author FeiYu
 * @date 2026-06-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "成员健康档案分页查询条件实体类")
public class DietUserHealthProfileQueryPO extends BasePageQuery {

    @Schema(description = "家庭组ID")
    private Long groupId;

    @Schema(description = "就餐成员姓名")
    private String name;
}
