package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色信息摘要 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "角色信息摘要")
public class SysRoleInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色主键")
    private Long roleId;

    @Schema(description = "角色名字")
    private String roleName;
}
