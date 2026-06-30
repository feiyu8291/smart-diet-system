package com.diet.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.model.entity.DietUserDishImage;
import com.diet.modules.common.config.AmzS3Config;
import com.diet.modules.common.enums.BucketEnum;
import com.diet.modules.system.mapper.SysFileStorageMapper;
import com.diet.modules.system.model.entity.SysFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 业务侧文件上传（主要是菜品图片）服务
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
@RequiredArgsConstructor
public class DietFileStorageService extends ServiceImpl<SysFileStorageMapper, SysFileStorage> {

    private final S3Client s3Client;
    private final AmzS3Config amzS3Config;
    private final DietUserDishImageService userDishImageService;

    @Transactional(rollbackFor = Exception.class)
    public String uploadDishImage(MultipartFile file, Long groupId, Long dishId, String creator) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件内容为空，上传失败");
        }

        String bucket = BucketEnum.SYSTEM_BASE_BUCKET.getBucketName();

        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(b -> b.bucket(bucket));
        }

        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }

        String realName = UUID.randomUUID() + suffix;

        try {
            s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(realName).contentType(file.getContentType()).build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件流失败", e);
        }

        String endpoint = amzS3Config.getEndPoint();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }
        String fileAccessUrl = endpoint + bucket + "/" + realName;

        SysFileStorage storage = new SysFileStorage();
        storage.setBucketNumber((short) 1);
        storage.setFileName(originalName);
        storage.setFileRealName(realName);
        storage.setFileSize(String.valueOf(file.getSize()));
        storage.setFileMimeType(file.getContentType() != null ? file.getContentType() : "image/jpeg");
        storage.setFileType(suffix.replace(".", ""));
        storage.setFilePath(fileAccessUrl);
        storage.setCreateBy(creator);
        storage.setUpdateBy(creator);
        this.baseMapper.insert(storage);

        Long storageId = storage.getStorageId();

        DietUserDishImage existingImage = userDishImageService.lambdaQuery()
                .eq(DietUserDishImage::getGroupId, groupId)
                .eq(DietUserDishImage::getDishId, dishId)
                .one();

        if (existingImage != null) {
            SysFileStorage oldStorage = this.baseMapper.selectById(existingImage.getStorageId());
            if (oldStorage != null) {
                oldStorage.setDelFlag(1);
                oldStorage.setUpdateTime(LocalDateTime.now());
                this.baseMapper.updateById(oldStorage);
            }

            existingImage.setStorageId(storageId);
            existingImage.setUpdateTime(LocalDateTime.now());
            existingImage.setUpdateBy(creator);
            userDishImageService.updateById(existingImage);
        } else {
            DietUserDishImage newImage = new DietUserDishImage();
            newImage.setGroupId(groupId);
            newImage.setDishId(dishId);
            newImage.setStorageId(storageId);
            newImage.setCreateBy(creator);
            newImage.setUpdateBy(creator);
            userDishImageService.save(newImage);
        }

        return fileAccessUrl;
    }
}

