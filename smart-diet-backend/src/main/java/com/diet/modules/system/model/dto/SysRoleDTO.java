package com.diet.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 角色 新增/修改 请求体
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "角色请求体")
public class SysRoleDTO {

    @Schema(description = "主键ID（修改时必传）")
    private Long roleId;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名字")
    private String roleName;

    @Schema(description = "角色说明备注")
    private String roleDescription;

    @Schema(description = "绑定的菜单ID列表")
    private List<Long> menuIds;
}
