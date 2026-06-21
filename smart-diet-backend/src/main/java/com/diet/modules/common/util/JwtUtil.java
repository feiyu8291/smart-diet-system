package com.diet.modules.common.util;

import com.diet.modules.auth.security.SecurityProperties;
import com.diet.modules.common.config.SpringBeanHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JWT 工具类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@UtilityClass
public class JwtUtil {

    private static final String CLAIM_USER_ID = "userId";

    private static final AtomicReference<SecurityProperties> propertiesCache = new AtomicReference<>();

    private static SecurityProperties getSecurityProperties() {
        SecurityProperties cached = propertiesCache.get();
        if (cached != null)
            return cached;
        synchronized (JwtUtil.class) {
            SecurityProperties second = propertiesCache.get();
            if (second == null) {
                SecurityProperties properties = SpringBeanHolder.getBean(SecurityProperties.class);
                propertiesCache.set(properties);
                return properties;
            }
            return second;
        }
    }

    /**
     * 生成 JWT Token
     */
    public static String generateToken(Long userId) {
        return Jwts.builder()
                .claim(CLAIM_USER_ID, userId)
                .setIssuedAt(new Date())
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token 获取 userId
     */
    public static Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get(CLAIM_USER_ID, Long.class);
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验 Token 签名是否合法
     */
    public static boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT 签名校验失败: {}", e.getMessage());
            return false;
        }
    }

    private static SecretKey getSecretKey() {
        byte[] keyBytes = getSecurityProperties().getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
