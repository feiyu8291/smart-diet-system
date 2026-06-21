package com.diet.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户简要信息 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户简要信息 DTO")
public class SysUserSimpleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phoneNum;
}
