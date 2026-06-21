package com.diet.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单信息表
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "菜单信息表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_menu")
public class SysMenu extends BaseEntity {

    @TableId(value = "menu_id", type = IdType.AUTO)
    @Schema(description = "菜单主键")
    private Long menuId;

    @TableField(value = "parent_id")
    @Schema(description = "父级菜单ID")
    private Long parentId;

    @TableField(value = "menu_name")
    @Schema(description = "菜单名字")
    private String menuName;

    @TableField(value = "menu_code")
    @Schema(description = "菜单code")
    private String menuCode;

    @TableField(value = "request_url")
    @Schema(description = "菜单url")
    private String requestUrl;

    @TableField(value = "menu_type")
    @Schema(description = "菜单类型(0目录 1菜单 2按钮 3权限 4外链 5其他)")
    private Short menuType;

    @TableField(value = "menu_icon")
    @Schema(description = "菜单图标")
    private String menuIcon;

    @TableField(value = "sort_order")
    @Schema(description = "排序")
    private Integer sortOrder;

    @TableField(value = "menu_description")
    @Schema(description = "描述")
    private String menuDescription;
}
