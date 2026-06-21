package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据字典分页列表 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "数据字典分页列表 VO")
public class SysDataDictPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long dictId;

    @Schema(description = "类型名称")
    private String dataTypeName;

    @Schema(description = "类型编码")
    private String dataType;

    @Schema(description = "数据编码")
    private String dataCode;

    @Schema(description = "数据值")
    private String dataValue;

    @Schema(description = "上级类型")
    private String parentType;

    @Schema(description = "上级编码")
    private String parentCode;

    @Schema(description = "字典备注")
    private String dataRemark;

    @Schema(description = "页面是否只读 0否 1是")
    private Integer webReadOnly;

    @Schema(description = "是否默认选中 0否 1是")
    private Integer defaultState;

    @Schema(description = "排序序号")
    private Integer dictSort;

    @Schema(description = "系统预置 0否 1是")
    private Integer initSystemFlag;
}
