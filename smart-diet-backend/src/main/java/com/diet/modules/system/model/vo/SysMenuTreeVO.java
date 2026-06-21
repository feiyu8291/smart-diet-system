package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 菜单树结构展示类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "菜单树结构展示类")
public class SysMenuTreeVO {

    @Schema(description = "菜单主键")
    private Long menuId;

    @Schema(description = "父级菜单ID")
    private Long parentId;

    @Schema(description = "菜单名字")
    private String menuName;

    @Schema(description = "菜单code")
    private String menuCode;

    @Schema(description = "菜单url")
    private String requestUrl;

    @Schema(description = "菜单类型(0目录 1菜单 2按钮 3权限 4外链 5其他)")
    private Short menuType;

    @Schema(description = "菜单图标")
    private String menuIcon;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "描述")
    private String menuDescription;

    @Schema(description = "子菜单列表")
    private List<SysMenuTreeVO> children;

    public void addChildren(SysMenuTreeVO child) {
        if (Objects.isNull(this.children)) {
            children = new ArrayList<>();
        }
        children.add(child);
    }
}
