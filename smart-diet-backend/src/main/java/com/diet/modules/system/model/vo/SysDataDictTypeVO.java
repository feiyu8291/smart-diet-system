package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据字典类型 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "系统数据字典表(类型)")
@Data
public class SysDataDictTypeVO implements Serializable {

    @Schema(description = "类型名字")
    private String dataTypeName;

    @Schema(description = "类型")
    private String dataType;
}
