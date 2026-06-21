package com.diet.modules.system.model.vo;

import com.diet.modules.system.model.entity.SysMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单 VO（含 children 树形结构）
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单树响应体")
public class SysMenuVO extends SysMenu {

    @Schema(description = "子菜单列表")
    private List<SysMenuVO> children;
}
