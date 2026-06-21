package com.diet.modules.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "登录请求体")
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "登录用户名/手机号/身份证")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码")
    private String userPassword;
}
