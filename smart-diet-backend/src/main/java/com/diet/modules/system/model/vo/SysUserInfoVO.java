package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 当前登录用户信息 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "当前登录用户信息")
public class SysUserInfoVO {

    @Schema(description = "用户主键ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "登录手机号")
    private String phoneNum;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}
