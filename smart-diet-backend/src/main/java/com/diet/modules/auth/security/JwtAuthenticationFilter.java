package com.diet.modules.auth.security;

import com.diet.modules.auth.service.AuthService;
import com.diet.modules.common.constant.CacheKeyConstant;
import com.diet.modules.common.entity.Result;
import com.diet.modules.common.entity.ResultCode;
import com.diet.modules.common.util.JsonNodeUtil;
import com.diet.modules.common.util.JwtUtil;
import com.diet.modules.common.util.RedisUtil;
import com.diet.modules.system.model.entity.SysUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * JWT 认证过滤器
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final AuthService authService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        boolean excluded = isExcluded(requestUri);
        if (excluded) {
            filterChain.doFilter(request, response);
            return;
        }
        String tokenHeader = securityProperties.getTokenHeader();
        String tokenPrefix = securityProperties.getTokenPrefix();
        String headerValue = request.getHeader(tokenHeader);
        if (!StringUtils.hasText(headerValue)) {
            String tokenParam = request.getParameter("token");
            if (StringUtils.hasText(tokenParam)) {
                headerValue = tokenPrefix + tokenParam;
            }
        }
        boolean hasToken = StringUtils.hasText(headerValue) && headerValue.startsWith(tokenPrefix);

        if (!hasToken) {
            writeUnauthorized(response, "请求头中缺少 Token");
            return;
        }

        String token = headerValue.substring(tokenPrefix.length());

        Long userId = JwtUtil.parseUserId(token);
        if (Objects.isNull(userId)) {
            writeUnauthorized(response, "Token 无效或签名异常");
            return;
        }

        String userCacheKey = CacheKeyConstant.USER_CACHE_PREFIX + userId;
        SysUser sysUser = RedisUtil.get(userCacheKey, SysUser.class);
        if (sysUser == null) {
            writeUnauthorized(response, "登录已失效，请重新登录");
            return;
        }

        Long expire = RedisUtil.getExpire(userCacheKey);
        String permCacheKey = CacheKeyConstant.USER_PERM_PREFIX + userId;
        if (expire != null && expire > 0 && expire < securityProperties.getTokenRenewThreshold()) {
            long newExpireSeconds = securityProperties.getTokenExpire();
            RedisUtil.expire(userCacheKey, newExpireSeconds);
            RedisUtil.expire(permCacheKey, newExpireSeconds);
            log.debug("Token 已自动续期，用户 [{}]，原剩余过期时间 {} 秒", userId, expire);
        }
        if (!RedisUtil.hasKey(permCacheKey)) {
            List<String> permUrlList = authService.loadUserPermissions(userId);
            long expireSeconds = securityProperties.getTokenExpire();
            RedisUtil.set(permCacheKey, permUrlList, expireSeconds);
        }
        List<String> permUrls = RedisUtil.getCacheList(permCacheKey, String.class);
        List<SimpleGrantedAuthority> authorities = permUrls == null
                ? Collections.emptyList()
                : permUrls.stream().map(SimpleGrantedAuthority::new).toList();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(sysUser, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("用户 [{}] 访问 [{}]", userId, requestUri);
        filterChain.doFilter(request, response);
    }

    private String cleanUri(String uri) {
        if (uri == null) {
            return "";
        }
        // 1. 剔除分号矩阵参数
        int semicolonIndex = uri.indexOf(';');
        if (semicolonIndex != -1) {
            uri = uri.substring(0, semicolonIndex);
        }
        // 2. 清理多重斜杠
        uri = uri.replaceAll("/{2,}", "/");
        // 3. 规范化路径折叠相对路径
        try {
            String path = new java.net.URI(uri).normalize().getPath();
            return path != null ? path : uri;
        } catch (Exception e) {
            return uri;
        }
    }

    private boolean isExcluded(String requestUri) {
        List<String> excludeUrls = securityProperties.getExcludeUrls();
        if (excludeUrls == null) return false;
        String cleanedUri = cleanUri(requestUri);
        return excludeUrls.stream().anyMatch(pattern -> pathMatcher.match(pattern, cleanedUri));
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result<Void> result = Result.failed(ResultCode.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(JsonNodeUtil.writeValueAsString(result));
    }
}
