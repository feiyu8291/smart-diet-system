package com.diet.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 用户 新增/修改 请求体
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "用户请求体")
public class SysUserDTO {

    @Schema(description = "用户主键ID（修改时必传）")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "登录手机号")
    private String phoneNum;

    @Schema(description = "用户密码（新增时必传）")
    private String userPassword;

    @NotBlank(message = "用户名账号不能为空")
    @Schema(description = "用户名账号")
    private String username;

    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证")
    private String idCardNum;

    @Schema(description = "绑定的角色ID列表")
    private List<Long> roleIds;
}
