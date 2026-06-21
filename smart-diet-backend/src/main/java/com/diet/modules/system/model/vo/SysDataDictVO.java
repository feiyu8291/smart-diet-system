package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "系统数据字典表(业务)")
@Data
@EqualsAndHashCode(callSuper = false)
public class SysDataDictVO implements Serializable {

    @Schema(description = "数据字典id")
    private Long dictId;

    @Schema(description = "类型名字")
    private String dataTypeName;

    @Schema(description = "类型")
    private String dataType;

    @Schema(description = "编码")
    private String dataCode;

    @Schema(description = "值")
    private String dataValue;

    @Schema(description = "上级类型")
    private String parentType;

    @Schema(description = "上级编码")
    private String parentCode;

    @Schema(description = "字典备注")
    private String dataRemark;

    @Schema(description = "页面是否只读0否1是")
    private Integer webReadOnly;

    @Schema(description = "是否默认选中0否1是")
    private Integer defaultState;

    @Schema(description = "排序")
    private Integer dictSort;

    @Schema(description = "逻辑删除0否1是")
    private Integer delFlag;

    @Schema(description = "子节点")
    private List<SysDataDictVO> childList = new ArrayList<>();
}
