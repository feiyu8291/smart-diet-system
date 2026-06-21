package com.diet.modules.system.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统-操作日志返回 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Accessors(chain = true)
@Schema(description = "操作日志返回对象")
public class SysOperationLogVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "操作IP")
    private String ipAddress;

    @Schema(description = "操作类型")
    private String opType;

    @Schema(description = "操作类型翻译")
    private String opTypeStr;

    @Schema(description = "操作模块")
    private String opModule;

    @Schema(description = "操作详情内容")
    private String content;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;

    public <T> void copy(T source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
