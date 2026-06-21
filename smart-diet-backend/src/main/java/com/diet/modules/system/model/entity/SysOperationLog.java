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
 * 系统操作日志表 实体类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "操作日志表")
@Data
@TableName(value = "sys_operation_log")
public class SysOperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField(value = "username")
    @Schema(description = "操作人用户名")
    private String username;

    @TableField(value = "real_name")
    @Schema(description = "操作人姓名")
    private String realName;

    @TableField(value = "ip_address")
    @Schema(description = "操作人IP地址")
    private String ipAddress;

    @TableField(value = "op_type")
    @Schema(description = "操作类型字典编码")
    private String opType;

    @TableField(value = "op_module")
    @Schema(description = "操作模块")
    private String opModule;

    @TableField(value = "content")
    @Schema(description = "操作描述详情")
    private String content;

    @TableField(value = "create_time")
    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
