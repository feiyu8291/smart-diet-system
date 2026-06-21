package com.diet.modules.auth.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Security 配置属性
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@Component
@ConfigurationProperties(prefix = "diet.security")
public class SecurityProperties {

    /**
     * token 有效期（秒），同时作为 Redis 缓存 TTL
     */
    private long tokenExpire = 7200;

    /**
     * Token 续期阈值（秒）：Redis TTL 低于此值时自动续期，默认 30 分钟
     */
    private long tokenRenewThreshold = 1800;

    /**
     * JWT 签名秘钥
     */
    private String secret;

    /**
     * Token 请求头名称
     */
    private String tokenHeader = "Authorization";

    /**
     * Token 前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 白名单 URL 列表（前缀匹配）
     */
    private List<String> excludeUrls;
}
