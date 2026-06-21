package com.diet.modules.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.common.config.AmzS3Config;
import com.diet.modules.common.config.SpringBeanHolder;
import com.diet.modules.common.constant.CharacterConstant;
import com.diet.modules.common.constant.PatternConstant;
import com.diet.modules.common.enums.BucketEnum;
import com.diet.modules.common.exception.BusinessException;
import com.diet.modules.system.mapper.SysFileStorageMapper;
import com.diet.modules.system.model.entity.SysFileStorage;
import com.diet.modules.system.model.po.SysFileDeleteDTO;
import com.diet.modules.system.model.po.SysStorageCriteria;
import com.diet.modules.system.model.vo.SysFileStorageBase64VO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统文件存储服务业务实现类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileStorageService extends ServiceImpl<SysFileStorageMapper, SysFileStorage> {

    private final S3Client s3Client;
    private final AmzS3Config amzS3Config;
    private final SysFileStorageMapper sysFileStorageMapper;

    private SysFileStorageService getSelf() {
        return SpringBeanHolder.getBean(SysFileStorageService.class);
    }

    public Page<SysFileStorage> queryAll(SysStorageCriteria criteria, Page<SysFileStorage> page) {
        Page<SysFileStorage> result = this.lambdaQuery()
                .like(StringUtils.hasText(criteria.getFileName()), SysFileStorage::getFileName, criteria.getFileName())
                .orderByDesc(SysFileStorage::getStorageId)
                .page(page);
        if (cn.hutool.core.collection.CollUtil.isNotEmpty(result.getRecords())) {
            result.getRecords().forEach(this::fillBucketName);
        }
        return result;
    }

    public List<SysFileStorage> queryAll(SysStorageCriteria criteria) {
        List<SysFileStorage> result = this.lambdaQuery()
                .like(StringUtils.hasText(criteria.getFileName()), SysFileStorage::getFileName, criteria.getFileName())
                .orderByDesc(SysFileStorage::getStorageId)
                .list();
        if (cn.hutool.core.collection.CollUtil.isNotEmpty(result)) {
            result.forEach(this::fillBucketName);
        }
        return result;
    }

    private void fillBucketName(SysFileStorage storage) {
        if (storage != null && storage.getBucketNumber() != null) {
            BucketEnum bucketEnum = BucketEnum.getBucketEnum(storage.getBucketNumber().intValue());
            if (bucketEnum != null) {
                storage.setBucketName(bucketEnum.getBucketName());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Collection<Long> ids, BucketEnum bucketEnum) {
        String bucketName = bucketEnum.getBucketName();
        boolean exists = bucketExists(bucketName);
        if (!exists) {
            log.warn("存储桶 '{}' 不可用或不存在，将跳过 S3 物理删除，仅清理数据库记录。", bucketName);
        }
        for (Long id : ids) {
            SysFileStorage storage = this.getById(id);
            if (Objects.isNull(storage) || storage.getFilePath().isEmpty()) {
                log.error("未找到 ID 为 {} 的文件记录，无法删除。", id);
                continue;
            }
            if (exists) {
                try {
                    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName).key(storage.getFilePath()).build();
                    s3Client.deleteObject(deleteObjectRequest);
                } catch (S3Exception e) {
                    log.error("从 S3 删除文件时出错: {}", e.awsErrorDetails().errorMessage(), e);
                }
            }
            sysFileStorageMapper.deleteById(id);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(List<SysFileDeleteDTO> dtoList) {
        if (CollUtil.isEmpty(dtoList)) {
            return;
        }
        for (SysFileDeleteDTO dto : dtoList) {
            SysFileStorage storage = this.getById(dto.getStorageId());
            if (Objects.isNull(storage) || CharSequenceUtil.isEmpty(storage.getFilePath())) {
                log.error("未找到 ID 为 {} 的文件记录，无法删除。", dto.getStorageId());
                continue;
            }
            String bucketName = dto.getBucketName();
            boolean exists = false;
            if (CharSequenceUtil.isNotEmpty(bucketName)) {
                exists = bucketExists(bucketName);
            }
            if (exists) {
                try {
                    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName).key(storage.getFilePath()).build();
                    s3Client.deleteObject(deleteObjectRequest);
                } catch (S3Exception e) {
                    log.error("从 S3 删除文件时出错, bucket: {}, key: {}, 错误信息: {}", bucketName, storage.getFilePath(),
                            e.awsErrorDetails().errorMessage(), e);
                }
            } else {
                log.warn("存储桶 '{}' 不可用或不存在，将跳过 S3 物理删除，仅清理数据库记录。", bucketName);
            }
            sysFileStorageMapper.deleteById(dto.getStorageId());
        }
    }

    public SysFileStorage upload(MultipartFile file, BucketEnum bucketEnum) {
        return this.upload(file, bucketEnum, null);
    }

    public SysFileStorage upload(MultipartFile file, BucketEnum bucketEnum, String customFileName) {
        try {
            String md5 = cn.hutool.crypto.SecureUtil.md5(file.getInputStream());
            SysFileStorage existing = this.lambdaQuery()
                    .eq(SysFileStorage::getFileMd5, md5)
                    .eq(SysFileStorage::getBucketNumber, bucketEnum.getBucketNumber())
                    .one();
            if (Objects.nonNull(existing)) {
                // 检查 S3 中是否确实存在此物理文件
                if (this.s3FileExists(bucketEnum.getBucketName(), existing.getFilePath())) {
                    log.info("检测到图片已存在，MD5：{}，且 S3 文件存在，复用现有 storageId：{}", md5, existing.getStorageId());
                    return existing;
                } else {
                    log.warn("检测到数据库中存在图片记录，MD5：{}，但 S3 中物理文件丢失，将重新上传至 S3，Key: {}", md5, existing.getFilePath());
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketEnum.getBucketName()).key(existing.getFilePath()).build();
                    s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
                    return existing;
                }
            }
            SysFileStorage storage = new SysFileStorage();
            this.initOrUpdateStorage(file, storage, bucketEnum, customFileName);
            storage.setFileMd5(md5);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketEnum.getBucketName()).key(storage.getFilePath()).build();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            sysFileStorageMapper.insert(storage);
            return storage;
        } catch (IOException e) {
            throw BusinessException.withMessageAndCause("文件上传失败", e);
        }
    }

    private boolean s3FileExists(String bucketName, String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName).key(key).build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("检查 S3 文件是否存在出错, bucket: {}, key: {}, 错误信息: {}", bucketName, key, e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    private void initOrUpdateStorage(MultipartFile file, SysFileStorage storage, BucketEnum bucketEnum, String customFileName) {
        String originalName = file.getOriginalFilename();
        if (CharSequenceUtil.isBlank(originalName)) {
            throw BusinessException.withMessage("文件名不能为空");
        }
        String folder = DateUtil.format(new Date(), amzS3Config.getTimeFormat());
        String ext = FileUtil.extName(originalName);
        String fileName;
        if (CharSequenceUtil.isNotBlank(customFileName)) {
            fileName = customFileName.trim() + "_" + IdUtil.simpleUUID() + CharacterConstant.DOT + ext;
        } else {
            fileName = IdUtil.simpleUUID() + CharacterConstant.DOT + ext;
        }
        String filePath = folder + CharacterConstant.SLASH + fileName;

        // 计算完整的文件外链地址（后端预览直链）
        String endpoint = amzS3Config.getDomain();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }
        String fileAccessUrl = endpoint + bucketEnum.getBucketName() + "/" + filePath;

        storage.setFileMimeType(FileUtil.getMimeType(originalName));
        storage.setFileName(originalName);
        storage.setFileRealName(fileName);
        storage.setFileSize(FileUtil.readableFileSize(file.getSize()));
        storage.setFileType(ext);
        storage.setFilePath(filePath); // 在数据库中保存相对 key，或者完整 url
        storage.setBucketNumber(bucketEnum.getBucketNumber().shortValue());

        // 兼容原有的前端预览直链字段（目标项目很多地方依赖 filePath 字段读取图片外链）
        // 如果 target 项目前端用 filePath 渲染图片，我们在此处把它设置成 fileAccessUrl 才能让图片正确展示
        storage.setFilePath(fileAccessUrl);
    }

    public void initBucket(String bucketName) {
        log.info("初始化存储桶：{}", bucketName);
        if (this.bucketExists(bucketName)) {
            log.info("存储桶 {} 已存在。", bucketName);
            return;
        }
        log.warn("存储桶 {} 不存在，尝试创建...", bucketName);
        if (!this.createBucket(bucketName)) {
            throw BusinessException.withMessage("存储桶创建失败，请检查配置或权限。");
        }
    }

    public void download(List<SysFileStorage> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysFileStorage storage : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("文件名称", storage.getFileName());
            map.put("真实存储的名称", storage.getFileRealName());
            map.put("文件大小", storage.getFileSize());
            map.put("文件MIME类型", storage.getFileMimeType());
            map.put("文件类型", storage.getFileType());
            map.put("文件路径", storage.getFilePath());
            map.put("创建者", storage.getCreateBy());
            map.put("更新者", storage.getUpdateBy());
            map.put("创建日期", storage.getCreateTime());
            map.put("更新时间", storage.getUpdateTime());
            list.add(map);
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(list.toString());
    }

    @SuppressWarnings("all")
    private boolean bucketExists(String bucketName) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
            s3Client.headBucket(headBucketRequest);
            return true;
        } catch (S3Exception e) {
            log.error("检查存储桶 '{}' 出错，状态码: {}，错误信息: {}", bucketName, e.statusCode(), e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    private boolean createBucket(String bucketName) {
        try {
            S3Waiter s3Waiter = s3Client.waiter();
            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName).build();
            s3Client.createBucket(bucketRequest);
            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder().bucket(bucketName).build();
            WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
            waiterResponse.matched().response()
                    .ifPresent(resp -> log.info("存储桶 '{}' 创建成功，状态: {}", bucketName, resp.sdkHttpResponse().statusCode()));
        } catch (BucketAlreadyOwnedByYouException e) {
            log.warn("存储桶 '{}' 已经被您拥有，无需重复创建。", bucketName);
        } catch (S3Exception e) {
            throw BusinessException.withMessage("创建存储桶时出错: " + e.awsErrorDetails().errorMessage());
        }
        return true;
    }

    public Map<String, String> privateDownload(String storageId) {
        SysFileStorage storage = sysFileStorageMapper.selectById(storageId);
        if (storage == null) {
            throw BusinessException.withMessage("文件不存在或已被删除");
        }
        String base64Data = this.getFileBase64ByStorage(storage);
        Map<String, String> responseData = new HashMap<>();
        responseData.put("fileName", storage.getFileName());
        responseData.put("fileMimeType", storage.getFileMimeType());
        responseData.put("base64Data", base64Data);
        return responseData;
    }

    public String getFilePathById(Long storageId) {
        if (Objects.isNull(storageId)) {
            return CharacterConstant.EMPTY;
        }
        SysFileStorage storage = sysFileStorageMapper.selectById(storageId);
        if (Objects.isNull(storage)) {
            return CharacterConstant.EMPTY;
        }
        String baseData = this.getFileBase64ByStorage(storage);
        return PatternConstant.BASE64DATA_PREFIX + baseData;
    }

    public String getFileBase64ByStorage(SysFileStorage storage) {
        BucketEnum bucketEnum = BucketEnum.getBucketEnum(storage.getBucketNumber().intValue());
        String key = storage.getFileRealName();
        // 如果是以相对路径存在，也可以通过相对路径获取
        if (storage.getFilePath().contains(storage.getFileRealName()) && !storage.getFilePath().startsWith("http")) {
            key = storage.getFilePath();
        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketEnum.getBucketName()).key(key).build();
        try (ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(getObjectRequest)) {
            byte[] fileBytes = IoUtil.readBytes(s3InputStream);
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (S3Exception e) {
            throw BusinessException.withMessage("从 S3 下载文件时出错: " + e.awsErrorDetails().errorMessage());
        } catch (IOException e) {
            throw BusinessException.withMessageAndCause("读取 S3 输入流时出错", e);
        }
    }

    public Map<Long, String> getFilePathMapByIds(List<String> storageIdList) {
        List<SysFileStorage> storageList = this.getListByStorageIds(storageIdList);
        return storageList.stream()
                .collect(Collectors.toMap(SysFileStorage::getStorageId, SysFileStorage::getFilePath, (k1, k2) -> k2));
    }

    public List<SysFileStorage> getListByStorageIds(List<String> storageIdList) {
        if (CollUtil.isEmpty(storageIdList)) {
            return new ArrayList<>();
        }
        return this.lambdaQuery().in(SysFileStorage::getStorageId, storageIdList).list();
    }

    public SysFileStorage getOneByStorageId(Long storageId) {
        if (Objects.isNull(storageId)) {
            return null;
        }
        return this.lambdaQuery().eq(SysFileStorage::getStorageId, storageId).one();
    }

    public SysFileStorage updateSingleFile(MultipartFile file, Long storageId, BucketEnum bucketEnum) {
        SysFileStorage storage = this.getOneByStorageId(storageId);
        if (Objects.isNull(storage)) {
            return this.upload(file, bucketEnum);
        }
        String bucketName = bucketEnum.getBucketName();
        try {
            this.deleteOldS3File(bucketName, storage.getFileRealName());
            this.initOrUpdateStorage(file, storage, bucketEnum, null);
            this.uploadFileToS3(file, bucketName, storage.getFileRealName());
            this.getSelf().saveOrUpdate(storage);
        } catch (S3Exception | IOException e) {
            log.error("更新 S3 文件时出错: {}", e.getMessage(), e);
        }
        return storage;
    }

    private void deleteOldS3File(String bucketName, String oldFilePath) {
        if (StringUtils.hasText(oldFilePath)) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName).key(oldFilePath).build();
            s3Client.deleteObject(deleteRequest);
            log.info("S3旧文件删除成功，路径：{}", oldFilePath);
        }
    }

    private void uploadFileToS3(MultipartFile file, String bucketName, String s3Key) throws IOException {
        PutObjectRequest uploadRequest = PutObjectRequest.builder()
                .bucket(bucketName).key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();
        s3Client.putObject(uploadRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    public List<SysFileStorage> uploadBatch(List<MultipartFile> fileList, BucketEnum bucketEnum) {
        return this.getSelf().updateBatchFile(fileList, null, bucketEnum);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysFileStorage> updateBatchFile(List<MultipartFile> fileList, List<String> storageIdList, BucketEnum bucketEnum) {
        List<SysFileStorage> storageList = this.getListByStorageIds(storageIdList);
        List<SysFileStorage> result = new ArrayList<>();
        int fileCount = fileList.size();
        updateMatchedFiles(fileList, storageList, bucketEnum, result);
        addExtraFiles(fileList, storageList, bucketEnum, result);
        deleteExtraStorages(storageList, fileCount, bucketEnum);
        return result;
    }

    private void updateMatchedFiles(List<MultipartFile> fileList, List<SysFileStorage> storageList,
                                    BucketEnum bucketEnum, List<SysFileStorage> result) {
        int updateCount = Math.min(fileList.size(), storageList.size());
        for (int i = 0; i < updateCount; i++) {
            SysFileStorage updated = this.updateSingleFile(fileList.get(i), storageList.get(i).getStorageId(), bucketEnum);
            result.add(updated);
        }
    }

    private void addExtraFiles(List<MultipartFile> fileList, List<SysFileStorage> storageList,
                               BucketEnum bucketEnum, List<SysFileStorage> result) {
        for (int i = storageList.size(); i < fileList.size(); i++) {
            SysFileStorage newStorage = this.upload(fileList.get(i), bucketEnum);
            result.add(newStorage);
        }
    }

    private void deleteExtraStorages(List<SysFileStorage> storageList, int fileCount, BucketEnum bucketEnum) {
        if (storageList.size() > fileCount) {
            List<Long> idsToDelete = storageList.stream()
                    .skip(fileCount)
                    .map(SysFileStorage::getStorageId)
                    .collect(Collectors.toList());
            this.getSelf().deleteAll(idsToDelete, bucketEnum);
        }
    }

    public void writeToOutputStream(SysFileStorage storage, HttpServletResponse response) {
        BucketEnum bucketEnum = BucketEnum.getBucketEnum(storage.getBucketNumber().intValue());
        String key = storage.getFileRealName();
        if (storage.getFilePath().contains(storage.getFileRealName()) && !storage.getFilePath().startsWith("http")) {
            key = storage.getFilePath();
        }
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketEnum.getBucketName()).key(key).build();
        try (ResponseInputStream<?> inputStream = s3Client.getObject(request);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            log.error("写入输出流时出错: {}", e.getMessage(), e);
        }
    }

    public List<SysFileStorageBase64VO> getBatchBase64(List<Long> storageIds) {
        if (CollUtil.isEmpty(storageIds)) {
            return Collections.emptyList();
        }
        List<SysFileStorage> storages = this.lambdaQuery().in(SysFileStorage::getStorageId, storageIds).list();
        return storages.stream().map(storage -> {
            try {
                String base64 = this.getFileBase64ByStorage(storage);
                SysFileStorageBase64VO vo = new SysFileStorageBase64VO();
                vo.setStorageId(String.valueOf(storage.getStorageId()));
                vo.setFileMimeType(storage.getFileMimeType());
                vo.setBase64Data(base64);
                return vo;
            } catch (Exception e) {
                log.error("批量转 base64 失败，storageId: {}", storage.getStorageId(), e);
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }
}
