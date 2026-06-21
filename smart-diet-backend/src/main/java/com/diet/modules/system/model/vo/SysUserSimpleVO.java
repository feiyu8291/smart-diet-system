package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统-用户简要信息 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户简要信息")
public class SysUserSimpleVO {

    @Schema(description = "用户主键ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;
}
