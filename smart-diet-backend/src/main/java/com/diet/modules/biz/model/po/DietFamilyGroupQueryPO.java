package com.diet.modules.biz.model.po;

import com.diet.modules.common.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家庭组分页查询条件 PO
 *
 * @author FeiYu
 * @date 2026-06-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "家庭组分页查询条件实体类")
public class DietFamilyGroupQueryPO extends BasePageQuery {

    @Schema(description = "家庭组名称")
    private String groupName;

    @Schema(description = "创建人用户ID")
    private Long creatorUserId;
}
