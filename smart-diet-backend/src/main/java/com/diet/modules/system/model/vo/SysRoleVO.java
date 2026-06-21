package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色信息VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "角色信息VO")
public class SysRoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色主键")
    private Long roleId;

    @Schema(description = "角色名字")
    private String roleName;

    @Schema(description = "角色说明备注")
    private String roleDescription;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
