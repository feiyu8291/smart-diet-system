package com.diet.modules.common.config;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * 亚马逊 S3 协议云存储配置属性类与 S3Client Bean 生成配置类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "amz.s3")
public class AmzS3Config {

    private String region;

    private String endPoint;

    private String domain;

    private String backendSever;

    private String accessKey;

    private String secretKey;

    private String defaultBucket;

    private String timeFormat;

    private boolean pathStyleAccess = true;

    @Bean
    public S3Client s3Client() {
        if (CharSequenceUtil.isBlank(endPoint)) {
            throw new IllegalArgumentException("S3 / MinIO 终结点（endPoint）未配置，请检查配置文件中的 amz.s3.endPoint");
        }
        if (CharSequenceUtil.isBlank(accessKey) || CharSequenceUtil.isBlank(secretKey)) {
            throw new IllegalArgumentException("S3 / MinIO 认证信息（accessKey/secretKey）未配置，请检查配置文件");
        }
        return S3Client.builder()
                .endpointOverride(URI.create(endPoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(CharSequenceUtil.isNotBlank(region) ? Region.of(region) : Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyleAccess)
                        .build())
                .build();
    }
}
