package com.diet.modules.system.model.po;

import com.diet.modules.common.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色查询PO")
public class SysRoleQueryPO extends BasePageQuery {
    @Schema(description = "角色名字")
    private String roleName;
}
