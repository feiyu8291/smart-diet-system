package com.diet.modules.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 亚马逊 S3 协议云存储配置属性类
 * (注释掉 S3Client Bean 的产生以防同名/同类型 Bean 冲突)
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

    /*
    @Bean
    public S3Client amazonS3Client() {
        ...
    }
    */
}
