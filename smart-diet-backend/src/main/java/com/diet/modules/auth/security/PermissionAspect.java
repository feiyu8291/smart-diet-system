package com.diet.modules.auth.security;

import com.diet.modules.common.entity.ResultCode;
import com.diet.modules.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collection;

/**
 * 权限校验切面类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Around("@annotation(requiresPermission)")
    public Object around(ProceedingJoinPoint point, RequiresPermission requiresPermission) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw BusinessException.withStateEnum(ResultCode.FORBIDDEN);
        }

        HttpServletRequest request = attributes.getRequest();
        String requestUri = request.getRequestURI();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw BusinessException.withStateEnum(ResultCode.UNAUTHORIZED);
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean hasPermission = false;

        String[] requiredPermissions = requiresPermission.value();
        if (requiredPermissions != null && requiredPermissions.length > 0) {
            for (String perm : requiredPermissions) {
                if (authorities.stream().anyMatch(a -> a.getAuthority().equals(perm))) {
                    hasPermission = true;
                    break;
                }
            }
        } else {
            String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String targetPath = bestMatchPattern != null ? bestMatchPattern : request.getRequestURI();

            hasPermission = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(pattern -> targetPath.equals(pattern) || pathMatcher.match(pattern, targetPath));
        }

        if (!hasPermission) {
            log.warn("用户无权访问接口: {}", requestUri);
            throw BusinessException.withMessageAndCode("无权访问此接口", ResultCode.FORBIDDEN.getCode());
        }

        return point.proceed();
    }
}
