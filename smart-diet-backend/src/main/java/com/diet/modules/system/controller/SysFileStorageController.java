package com.diet.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diet.modules.common.entity.Result;
import com.diet.modules.system.model.entity.SysFileStorage;
import com.diet.modules.system.model.po.SysFileDeleteDTO;
import com.diet.modules.system.model.po.SysStorageCriteria;
import com.diet.modules.system.model.vo.SysFileStorageBase64VO;
import com.diet.modules.system.model.vo.SysFileStorageVO;
import com.diet.modules.system.service.SysFileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * S3 协议云存储管理 Controller
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3Storage")
@Tag(name = "工具：S3协议云存储管理")
public class SysFileStorageController {

    private final SysFileStorageService sysFileStorageService;

    @Operation(summary = "导出数据")
    @GetMapping(value = "/export")
    public void exportStorage(HttpServletResponse response, SysStorageCriteria criteria) throws IOException {
        sysFileStorageService.download(sysFileStorageService.queryAll(criteria), response);
    }

    @GetMapping
    @Operation(summary = "查询文件列表")
    public Result<Page<SysFileStorage>> queryStorage(SysStorageCriteria criteria) {
        Page<SysFileStorage> page = new Page<>(criteria.getPage(), criteria.getSize());
        return Result.success(sysFileStorageService.queryAll(criteria, page));
    }

    @PostMapping(value = "/upload")
    @Operation(summary = "上传文件")
    public Result<SysFileStorageVO> uploadStorage(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "存储桶名称") @RequestParam("bucketName") String bucketName,
            @Parameter(description = "自定义文件名称(可选)") @RequestParam(value = "customName", required = false) String customName,
            @Parameter(description = "自定义文件名称-驼峰(可选)") @RequestParam(value = "customFileName", required = false) String customFileName) {
        String finalCustomName = cn.hutool.core.text.CharSequenceUtil.isNotBlank(customFileName) ? customFileName : customName;
        com.diet.modules.common.enums.BucketEnum bucket = com.diet.modules.common.enums.BucketEnum.getBucketEnum(bucketName);
        if (bucket == null) {
            bucket = com.diet.modules.common.enums.BucketEnum.FILE_BUCKET;
        }
        SysFileStorage storage = sysFileStorageService.upload(file, bucket, finalCustomName);
        SysFileStorageVO vo = new SysFileStorageVO()
                .setStorageId(storage.getStorageId())
                .setFileName(storage.getFileName())
                .setFileType(storage.getFileType())
                .setFileSize(storage.getFileSize())
                .setUrl(storage.getFilePath());
        return Result.success(vo);
    }

    @PostMapping("/batch-base64")
    @Operation(summary = "批量获取文件base64文本数据")
    public Result<List<SysFileStorageBase64VO>> getBatchBase64(@RequestBody List<Long> storageIds) {
        return Result.success(sysFileStorageService.getBatchBase64(storageIds));
    }

    @Operation(summary = "获取文件信息")
    @GetMapping(value = "/info/{storageId}")
    public Result<SysFileStorageVO> getFileInfo(@PathVariable Long storageId) {
        SysFileStorage storage = sysFileStorageService.getOneByStorageId(storageId);
        if (storage == null) {
            return Result.failed("文件不存在或已被删除");
        }
        SysFileStorageVO vo = new SysFileStorageVO()
                .setStorageId(storage.getStorageId())
                .setFileName(storage.getFileName())
                .setFileType(storage.getFileType())
                .setFileSize(storage.getFileSize())
                .setUrl(storage.getFilePath());
        return Result.success(vo);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除多个文件")
    public Result<Void> deleteAllStorage(@RequestBody @Valid List<SysFileDeleteDTO> dtoList) {
        sysFileStorageService.deleteAll(dtoList);
        return Result.success();
    }

    @Operation(summary = "预览文件")
    @GetMapping("/preview/{storageId}")
    public void preview(@PathVariable Long storageId, HttpServletResponse response) throws Exception {
        SysFileStorage storage = sysFileStorageService.getOneByStorageId(storageId);
        if (storage == null || storage.getFilePath() == null) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "文件不存在");
            return;
        }
        response.setContentType(storage.getFileMimeType());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        sysFileStorageService.writeToOutputStream(storage, response);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{storageId}")
    public void download(@PathVariable Long storageId, HttpServletResponse response) throws Exception {
        SysFileStorage storage = sysFileStorageService.getOneByStorageId(storageId);
        if (storage == null || storage.getFilePath() == null) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "文件不存在");
            return;
        }
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + URLEncoder.encode(storage.getFileName(), StandardCharsets.UTF_8));
        sysFileStorageService.writeToOutputStream(storage, response);
    }
}
