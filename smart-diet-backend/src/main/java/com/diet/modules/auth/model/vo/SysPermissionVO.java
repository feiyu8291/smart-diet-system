package com.diet.modules.auth.model.vo;

import com.diet.modules.system.model.entity.SysUser;
import com.diet.modules.system.model.vo.SysMenuTreeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 系统权限 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Setter
@Getter
@Accessors(chain = true)
@Schema(description = "系统权限VO")
public class SysPermissionVO {

    @Schema(description = "当前用户")
    private SysUser currentUser;

    @Schema(description = "菜单树列表")
    private List<SysMenuTreeVO> menuTreeList;

    @Schema(description = "权限URL列表")
    private List<String> permUrls;

    @Schema(description = "菜单权限标识列表")
    private List<String> menuCodes;
}
