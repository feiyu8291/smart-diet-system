package com.diet.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典表
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "数据字典表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_data_dictionary")
public class SysDataDictionary extends BaseEntity {

    @TableId(value = "dict_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long dictId;

    @TableField(value = "data_type_name")
    @Schema(description = "类型名字")
    private String dataTypeName;

    @TableField(value = "data_type")
    @Schema(description = "类型")
    private String dataType;

    @TableField(value = "data_code")
    @Schema(description = "编码")
    private String dataCode;

    @TableField(value = "data_value")
    @Schema(description = "值")
    private String dataValue;

    @TableField(value = "parent_type")
    @Schema(description = "上级类型")
    private String parentType;

    @TableField(value = "parent_code")
    @Schema(description = "上级编码")
    private String parentCode;

    @TableField(value = "data_remark")
    @Schema(description = "字典备注")
    private String dataRemark;

    @TableField(value = "web_read_only")
    @Schema(description = "页面是否只读 0否 1是")
    private Integer webReadOnly;

    @TableField(value = "default_state")
    @Schema(description = "是否默认选中 0否 1是")
    private Integer defaultState;

    @TableField(value = "dict_sort")
    @Schema(description = "排序")
    private Integer dictSort;

    @TableField(value = "init_system_flag")
    @Schema(description = "初始化系统标志 0否 1是 系统初始化的数据不支持删除")
    private Integer initSystemFlag;
}
