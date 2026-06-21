package com.diet.modules.system.model.po;

import com.diet.modules.common.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询参数 PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户分页查询对象")
public class SysUserQueryPO extends BasePageQuery {

    @Schema(description = "真实姓名（模糊查询）")
    private String realName;

    @Schema(description = "手机号（模糊查询）")
    private String phoneNum;
}
