package com.diet.modules.system.model.po;

import com.diet.modules.common.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典查询PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据字典查询PO")
public class SysDataDictionaryQueryPO extends BasePageQuery {

    @Schema(description = "类型")
    private String dataType;

    @Schema(description = "类型名字")
    private String dataTypeName;

    @Schema(description = "编码")
    private String dataCode;

    @Schema(description = "值")
    private String dataValue;
}
