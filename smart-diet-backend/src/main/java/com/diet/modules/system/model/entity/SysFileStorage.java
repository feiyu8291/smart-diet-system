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
 * 系统文件存储表（S3协议对象存储）
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Schema(description = "系统文件存储表（S3协议对象存储）")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_file_storage")
public class SysFileStorage extends BaseEntity {

    @TableId(value = "storage_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long storageId;

    @TableField(value = "bucket_number")
    @Schema(description = "存储桶编号")
    private Short bucketNumber;

    @TableField(value = "file_name")
    @Schema(description = "文件名称")
    private String fileName;

    @TableField(value = "file_real_name")
    @Schema(description = "真实存储 of S3名称")
    private String fileRealName;

    @TableField(value = "file_size")
    @Schema(description = "文件大小")
    private String fileSize;

    @TableField(value = "file_mime_type")
    @Schema(description = "文件MIME类型")
    private String fileMimeType;

    @TableField(value = "file_type")
    @Schema(description = "文件类型")
    private String fileType;

    @TableField(value = "file_path")
    @Schema(description = "文件路径")
    private String filePath;

    @TableField(value = "file_md5")
    @Schema(description = "文件的MD5哈希值")
    private String fileMd5;

    @TableField(exist = false)
    @Schema(description = "存储桶名称")
    private String bucketName;
}
