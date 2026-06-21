package com.diet.modules.common.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公共基础实体类
 * <p>
 * 包含五个公共字段：del_flag、create_by、update_by、create_time、update_time
 * </p>
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "公共基础实体")
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableLogic
    @TableField(value = "del_flag")
    @Schema(description = "是否无效：0-有效 1-已删除", example = "0")
    @JsonIgnore
    private Integer delFlag;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @Schema(description = "创建人ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String updateBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "修改时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updateTime;

    public <T> void copy(T source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
