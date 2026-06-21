package com.diet.modules.common.constant;

import lombok.experimental.UtilityClass;

/**
 * 缓存键常量
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@UtilityClass
public class CacheKeyConstant {

    /**
     * Redis Key 前缀：用户信息缓存 用于在线会话校验
     */
    public static final String USER_CACHE_PREFIX = "sys:user:cache:";

    /**
     * Redis Key 前缀：用户权限(菜单URL/按钮)缓存
     */
    public static final String USER_PERM_PREFIX = "sys:perm:cache:";

    /**
     * 业务数据字典全量
     */
    public static final String DICT_ALL = "dict::list_all";

    /**
     * Redis Key 前缀：用户简要信息缓存
     */
    public static final String USER_SIMPLE_CACHE_PREFIX = "sys:user:simple:";

    /**
     * 缓存过期时间 (秒) - 2小时
     */
    public static final long REDIS_KEY_EXPIRE_2_HOUR = 2 * 60 * 60L;

    /**
     * 缓存过期时间 (秒) - 12小时
     */
    public static final long REDIS_KEY_EXPIRE_12_HOUR = 12 * 60 * 60L;

    /**
     * 缓存过期时间 (秒) - 1周
     */
    public static final long REDIS_KEY_EXPIRE_7_DAY = 7 * 24 * 60 * 60L;

    /**
     * 资源 Base64 缓存前缀
     */
    public static final String RESOURCE_BASE64 = "resource::base64:";
}
