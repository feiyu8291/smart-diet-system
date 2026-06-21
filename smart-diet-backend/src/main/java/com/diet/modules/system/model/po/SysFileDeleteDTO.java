package com.diet.modules.system.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 文件存储删除请求对象
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Accessors(chain = true)
@Schema(description = "文件存储删除请求对象")
public class SysFileDeleteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "文件ID不能为空")
    @Schema(description = "主键ID")
    private Long storageId;

    @NotBlank(message = "存储桶名称不能为空")
    @Schema(description = "存储桶名称")
    private String bucketName;
}
