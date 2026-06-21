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
 * 角色信息表
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "角色信息表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role")
public class SysRole extends BaseEntity {

    @TableId(value = "role_id", type = IdType.AUTO)
    @Schema(description = "角色主键")
    private Long roleId;

    @TableField(value = "role_name")
    @Schema(description = "角色名字")
    private String roleName;

    @TableField(value = "role_description")
    @Schema(description = "角色说明备注")
    private String roleDescription;
}
