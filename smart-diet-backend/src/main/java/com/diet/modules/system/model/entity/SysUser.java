package com.diet.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户信息表
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "用户信息表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user")
public class SysUser extends BaseEntity {

    @TableId(value = "user_id", type = IdType.AUTO)
    @Schema(description = "用户主键ID")
    private Long userId;

    @TableField(value = "real_name")
    @Schema(description = "真实姓名")
    private String realName;

    @TableField(value = "phone_num")
    @Schema(description = "登录手机号")
    private String phoneNum;

    @JsonIgnore
    @TableField(value = "user_password")
    @Schema(description = "用户密码")
    private String userPassword;

    @TableField(value = "username")
    @Schema(description = "用户姓名")
    private String username;

    @TableField(value = "id_card_num")
    @Schema(description = "身份证")
    private String idCardNum;

    @TableField(value = "use_status")
    @Schema(description = "是否禁用 0否 1是")
    private Integer useStatus;
}
