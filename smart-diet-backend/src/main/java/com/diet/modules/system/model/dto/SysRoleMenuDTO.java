package com.diet.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色配置菜单请求体
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "角色配置菜单参数")
public class SysRoleMenuDTO {

    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @Schema(description = "菜单ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "菜单列表不能为空")
    private List<Long> menuIds;
}
