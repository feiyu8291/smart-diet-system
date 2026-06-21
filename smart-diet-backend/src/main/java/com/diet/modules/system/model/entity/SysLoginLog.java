package com.diet.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统登录日志表 实体类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "登录日志表")
@Data
@TableName(value = "sys_login_log")
public class SysLoginLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField(value = "username")
    @Schema(description = "用户名")
    private String username;

    @TableField(value = "real_name")
    @Schema(description = "真实姓名")
    private String realName;

    @TableField(value = "login_ip")
    @Schema(description = "登录IP地址")
    private String loginIp;

    @TableField(value = "login_time")
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    @TableField(value = "status")
    @Schema(description = "登录状态(0成功 1失败)")
    private Integer status;

    @TableField(value = "msg")
    @Schema(description = "返回消息提示")
    private String msg;
}
