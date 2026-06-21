package com.diet.modules.system.runner;

import com.diet.modules.common.enums.BucketEnum;
import com.diet.modules.system.service.SysFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 系统启动时自动检测并初始化 S3/MinIO 存储桶的启动器
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysFileBucketInitRunner implements ApplicationRunner {

    private final SysFileStorageService sysFileStorageService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始系统启动时初始化存储桶...");
        for (BucketEnum bucketEnum : BucketEnum.values()) {
            try {
                sysFileStorageService.initBucket(bucketEnum.getBucketName());
            } catch (Exception e) {
                log.error("初始化存储桶 '{}' 失败，原因: ", bucketEnum.getBucketName(), e);
            }
        }
        log.info("系统启动时初始化存储桶结束。");
    }
}
