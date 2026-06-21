package com.diet.modules.common.util;

import com.diet.modules.common.entity.ResultCode;
import com.diet.modules.common.exception.BusinessException;
import com.diet.modules.system.model.entity.SysUser;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 上下文工具类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@UtilityClass
public final class SecurityUtils {

    /**
     * 获取当前登录用户
     */
    public static SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw BusinessException.withStateEnum(ResultCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SysUser sysUser) {
            return sysUser;
        }
        throw BusinessException.withStateEnum(ResultCode.UNAUTHORIZED);
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}
