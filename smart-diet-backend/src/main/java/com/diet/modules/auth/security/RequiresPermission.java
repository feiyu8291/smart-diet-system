package com.diet.modules.auth.security;

import java.lang.annotation.*;

/**
 * 接口权限校验注解
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    /**
     * 权限标识符列表
     */
    String[] value() default {};
}
