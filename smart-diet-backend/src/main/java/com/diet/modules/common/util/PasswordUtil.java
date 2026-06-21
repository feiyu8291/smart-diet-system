package com.diet.modules.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密匹配工具类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@UtilityClass
public class PasswordUtil {

    private static volatile PasswordEncoder passwordEncoder;

    public static PasswordEncoder getInstance() {
        if (passwordEncoder == null) {
            synchronized (PasswordUtil.class) {
                if (passwordEncoder == null) {
                    passwordEncoder = new BCryptPasswordEncoder();
                }
            }
        }
        return passwordEncoder;
    }

    public static String encode(CharSequence rawPassword) {
        return getInstance().encode(rawPassword);
    }

    public static boolean matches(CharSequence rawPassword, String encodedPassword) {
        return getInstance().matches(rawPassword, encodedPassword);
    }

    public static boolean mismatch(CharSequence rawPassword, String encodedPassword) {
        return !matches(rawPassword, encodedPassword);
    }
}
