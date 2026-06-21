package com.diet.modules.common.constant;

import lombok.experimental.UtilityClass;

/**
 * 类文件描述：正则常量类
 *
 * @author Fei_Yu
 * @date 2025/12/16 11:09
 */
@UtilityClass
public class PatternConstant {

    // 日期格式化常量（保留框架原有）
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String CONTINUOUS_DATE_FORMAT = "yyyyMMdd";
    // 图片类型格式化常量
    public static final String IMAGE_TYPE_FORMAT = "gif jpg png jpeg";

    public static final String BASE64DATA_PREFIX = "data:image/png;base64,";
    /**
     * 外链前缀-HTTP（校验外链格式用）
     */
    public static final String HTTP_PREFIX = "http://";
    /**
     * 外链前缀-HTTPS（校验外链格式用）
     */
    public static final String HTTPS_PREFIX = "https://";
}