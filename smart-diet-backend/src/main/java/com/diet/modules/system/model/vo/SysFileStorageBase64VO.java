package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批量文件 Base64 转换返回 VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Schema(description = "批量文件Base64转换返回对象")
public class SysFileStorageBase64VO {

    @Schema(description = "附件存储ID")
    private String storageId;

    @Schema(description = "MIME类型")
    private String fileMimeType;

    @Schema(description = "文件Base64文本")
    private String base64Data;
}
