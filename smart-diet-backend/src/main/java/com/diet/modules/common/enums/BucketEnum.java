package com.diet.modules.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储桶枚举类（开发环境统一映射至唯一桶 smart-diet）
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Getter
public enum BucketEnum {

    SYSTEM_BASE_BUCKET("基础数据存储桶", 1, "smart-diet", 100),
    FILE_BUCKET("文件存储桶", 2, "smart-diet", 100);

    private final String bucketName;
    private final Integer bucketNumber;
    private final String bucketDesc;
    private final Integer fileMaxSizeMB;

    BucketEnum(String bucketDesc, Integer bucketNumber, String bucketName, Integer fileMaxSizeMB) {
        this.bucketDesc = bucketDesc;
        this.bucketNumber = bucketNumber;
        this.bucketName = bucketName;
        this.fileMaxSizeMB = fileMaxSizeMB;
    }

    private static final Map<String, BucketEnum> BUCKET_NAME_MAP = new HashMap<>();
    private static final Map<Integer, BucketEnum> BUCKET_NUMBER_MAP = new HashMap<>();

    static {
        for (BucketEnum bucketEnum : BucketEnum.values()) {
            BUCKET_NAME_MAP.put(bucketEnum.bucketName, bucketEnum);
            BUCKET_NUMBER_MAP.put(bucketEnum.bucketNumber, bucketEnum);
        }
    }

    public static BucketEnum getBucketEnum(String bucketName) {
        return BUCKET_NAME_MAP.get(bucketName);
    }

    public static BucketEnum getBucketEnum(Integer bucketNumber) {
        return BUCKET_NUMBER_MAP.get(bucketNumber);
    }
}
