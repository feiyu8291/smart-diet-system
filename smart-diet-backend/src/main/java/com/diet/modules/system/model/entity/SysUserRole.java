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
 * 系统-用户角色关联表
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "系统-用户角色关联表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_role")
public class SysUserRole extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField(value = "user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField(value = "role_id")
    @Schema(description = "角色ID")
    private Long roleId;
}
