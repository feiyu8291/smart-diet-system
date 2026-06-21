package com.diet.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 数据字典 新增/修改 请求体
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "数据字典请求体")
public class SysDataDictionaryDTO {

    @Schema(description = "主键ID（修改时必传）")
    private Long dictId;

    @Schema(description = "类型名字")
    private String dataTypeName;

    @NotBlank(message = "数据类型不能为空")
    @Schema(description = "类型")
    private String dataType;

    @NotBlank(message = "数据编码不能为空")
    @Schema(description = "编码")
    private String dataCode;

    @NotBlank(message = "数据值不能为空")
    @Schema(description = "值")
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

    @Schema(description = "排序")
    private Integer dictSort;
}
