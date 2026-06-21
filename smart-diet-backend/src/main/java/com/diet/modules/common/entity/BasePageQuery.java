package com.diet.modules.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分页查询基础公共类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class BasePageQuery {

    @NotNull(message = "页码不能为空")
    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNo = 1;

    @NotNull(message = "每页条数不能为空")
    @Schema(description = "每页条数", defaultValue = "10")
    private Integer pageSize = 10;
}
