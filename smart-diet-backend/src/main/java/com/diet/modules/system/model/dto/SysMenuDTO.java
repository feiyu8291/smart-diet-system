package com.diet.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单 新增/修改 请求体
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "菜单请求体")
public class SysMenuDTO {

    @Schema(description = "主键ID（修改时必传）")
    private Long menuId;

    @Schema(description = "父级菜单ID（顶级菜单无需传）")
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    @Schema(description = "菜单名字")
    private String menuName;

    @Schema(description = "菜单code")
    private String menuCode;

    @Schema(description = "菜单url")
    private String requestUrl;

    @NotNull(message = "菜单类型不能为空")
    @Schema(description = "菜单类型 1菜单 2按钮 3其他")
    private Short menuType;

    @Schema(description = "菜单图标")
    private String menuIcon;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "描述")
    private String menuDescription;
}
