package com.diet.modules.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件存储VO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Accessors(chain = true)
@Schema(description = "S3存储信息")
public class SysFileStorageVO {

    @Schema(description = "存储ID")
    private Long storageId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件大小")
    private String fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "URL")
    private String url;
}
