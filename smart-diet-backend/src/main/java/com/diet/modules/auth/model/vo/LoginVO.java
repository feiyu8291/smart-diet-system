package com.diet.modules.auth.model.vo;

import com.diet.modules.system.model.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Builder
@Schema(description = "登录响应体")
public class LoginVO {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "Token 过期时间")
    private LocalDateTime expireAt;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "登录手机号")
    private String loginPhone;

    @Schema(description = "权限信息")
    private List<String> permUrls;

    @Schema(description = "当前登录用户实体信息")
    private SysUser sysUser;
}
