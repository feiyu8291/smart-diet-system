package com.diet.modules.common.util;

import cn.hutool.core.collection.CollUtil;
import com.diet.modules.common.config.SpringBeanHolder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Redis 工具类（静态方法，懒加载，不直接依赖 Spring 容器，去除 Redisson 依赖）
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@UtilityClass
public class RedisUtil {

    @SuppressWarnings("rawtypes")
    private static final AtomicReference<RedisTemplate> templateCache = new AtomicReference<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static RedisTemplate<String, Object> getTemplate() {
        RedisTemplate cached = templateCache.get();
        if (cached != null) {
            return cached;
        }
        synchronized (RedisUtil.class) {
            RedisTemplate second = templateCache.get();
            if (second == null) {
                RedisTemplate<String, Object> newTemplate = SpringBeanHolder.getBean("redisTemplate", RedisTemplate.class);
                templateCache.set(newTemplate);
                return newTemplate;
            }
            return second;
        }
    }

    // ========== Key 操作 ==========

    public static boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(getTemplate().hasKey(key));
        } catch (Exception e) {
            log.error("hasKey 操作失败，key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    public static void del(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        getTemplate().delete(Arrays.asList(keys));
    }

    public static void delMore(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        getTemplate().delete(keys);
    }

    public static void delLikePrefix(String keyPrefix) {
        Collection<String> keys = getLikePrefixKeys(keyPrefix);
        if (CollUtil.isNotEmpty(keys)) {
            getTemplate().delete(keys);
        }
    }

    public static Collection<String> getLikePrefixKeys(String keyPrefix) {
        return getTemplate().keys(keyPrefix + "*");
    }

    public static void expire(String key, long seconds) {
        if (seconds <= 0) return;
        try {
            getTemplate().expire(key, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置过期时间失败，key={}, error={}", key, e.getMessage());
        }
    }

    public static Long getExpire(String key) {
        if (!hasKey(key)) {
            return -2L;
        }
        return getTemplate().getExpire(key, TimeUnit.SECONDS);
    }

    // ========== String 操作 ==========

    public static <T> T get(String key, Class<T> clazz) {
        if (isBlank(key)) {
            log.debug("Redis get操作：key为空，直接返回null");
            return null;
        }

        Object rawValue = getRedisValue(key);
        if (rawValue == null) {
            return null;
        }
        return convertByJson(rawValue, clazz);
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static Object getRedisValue(String key) {
        try {
            return getTemplate().opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis key [{}] 获取数据失败", key, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertByJson(Object item, Class<T> clazz) {
        if (item == null || clazz == null) {
            return null;
        }
        if (clazz == String.class && item instanceof String) {
            return (T) item;
        }
        try {
            String itemJson = item instanceof String sourceStr ? sourceStr : JsonNodeUtil.writeValueAsString(item);
            return JsonNodeUtil.readValue(itemJson, clazz);
        } catch (Exception ex) {
            log.error("Redis 元素 JSON 转换失败，元素：{}，目标类型：{}", item, clazz.getSimpleName(), ex);
            return null;
        }
    }

    public static <T> List<T> getCacheList(String key, Class<T> clazz) {
        if (isBlank(key) || clazz == null) return Collections.emptyList();
        Object obj = getRedisValue(key);
        if (obj == null) return Collections.emptyList();
        try {
            String jsonStr = obj instanceof String valueStr ? valueStr : JsonNodeUtil.writeValueAsString(obj);
            return JsonNodeUtil.readValueList(jsonStr, clazz);
        } catch (Exception e) {
            log.error("Redis key [{}] 转换 List 失败", key, e);
            return Collections.emptyList();
        }
    }

    public static void set(String key, Object value) {
        try {
            String jsonValue = value instanceof String valueStr ? valueStr : JsonNodeUtil.writeValueAsString(value);
            getTemplate().opsForValue().set(key, jsonValue);
        } catch (Exception e) {
            log.error("set 操作失败，key={}, error={}", key, e.getMessage());
        }
    }

    public static void set(String key, Object value, long seconds) {
        try {
            String jsonValue = value instanceof String valueStr ? valueStr : JsonNodeUtil.writeValueAsString(value);
            if (seconds > 0) {
                getTemplate().opsForValue().set(key, jsonValue, seconds, TimeUnit.SECONDS);
            } else {
                getTemplate().opsForValue().set(key, jsonValue);
            }
        } catch (Exception e) {
            log.error("set with expire 操作失败, key={}, error={}", key, e.getMessage());
        }
    }

    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                getTemplate().opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public static long increment(String key, long delta) {
        Long result = getTemplate().opsForValue().increment(key, delta);
        return result == null ? 0L : result;
    }

    // ========== Hash 操作 ==========

    @SuppressWarnings("unchecked")
    public static <T> T getHash(String key, String item) {
        Object obj = getTemplate().opsForHash().get(key, item);
        if (obj == null) return null;
        try {
            return (T) obj;
        } catch (ClassCastException e) {
            String jsonStr = JsonNodeUtil.writeValueAsString(obj);
            return (T) JsonNodeUtil.readValue(jsonStr, obj.getClass());
        }
    }

    public static Map<Object, Object> getHashMap(String key) {
        return getTemplate().opsForHash().entries(key);
    }

    public static void setHashMap(String key, Map<String, Object> map) {
        getTemplate().opsForHash().putAll(key, map);
    }

    public static void setHashMap(String key, Map<String, Object> map, long seconds) {
        getTemplate().opsForHash().putAll(key, map);
        if (seconds > 0) {
            expire(key, seconds);
        }
    }

    public static void setHash(String key, String item, Object value) {
        try {
            getTemplate().opsForHash().put(key, item, value);
        } catch (Exception e) {
            log.error("setHash 操作失败，key={}, item={}, error={}", key, item, e.getMessage());
        }
    }

    public static void delHash(String key, Object... items) {
        getTemplate().opsForHash().delete(key, items);
    }

    public static boolean hasHashKey(String key, String item) {
        return getTemplate().opsForHash().hasKey(key, item);
    }

    public static double incrementHash(String key, String item, double delta) {
        return getTemplate().opsForHash().increment(key, item, delta);
    }

    // ========== Set 操作 ==========

    public static Set<Object> getSet(String key) {
        try {
            return getTemplate().opsForSet().members(key);
        } catch (Exception e) {
            log.error("getSet 操作失败，key={}, error={}", key, e.getMessage());
            return new HashSet<>();
        }
    }

    public static boolean hasSetMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(getTemplate().opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("hasSetMember 操作失败，key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    public static long addSet(String key, Object... values) {
        try {
            Long result = getTemplate().opsForSet().add(key, values);
            return result == null ? 0L : result;
        } catch (Exception e) {
            log.error("addSet 操作失败，key={}, error={}", key, e.getMessage());
            return 0L;
        }
    }

    public static long removeSet(String key, Object... values) {
        try {
            Long result = getTemplate().opsForSet().remove(key, values);
            return result == null ? 0L : result;
        } catch (Exception e) {
            log.error("removeSet 操作失败，key={}, error={}", key, e.getMessage());
            return 0L;
        }
    }

    public static long getSetSize(String key) {
        try {
            Long size = getTemplate().opsForSet().size(key);
            return size == null ? 0L : size;
        } catch (Exception e) {
            log.error("getSetSize 操作失败，key={}, error={}", key, e.getMessage());
            return 0L;
        }
    }

    // ========== List 操作 ==========

    public static <T> List<T> getList(String key, long start, long end, Class<T> clazz) {
        if (isBlank(key) || clazz == null) {
            return Collections.emptyList();
        }
        List<Object> redisList = getTemplate().opsForList().range(key, start, end);
        if (redisList == null || redisList.isEmpty()) {
            log.debug("Redis key [{}] 范围 [{},{}] 无数据", key, start, end);
            return Collections.emptyList();
        }
        try {
            return redisList.stream().map(item -> convertByJson(item, clazz)).toList();
        } catch (Exception e) {
            log.warn("Redis List 直接转换失败，尝试 JSON 批量转换", e);
            try {
                String jsonStr = JsonNodeUtil.writeValueAsString(redisList);
                return JsonNodeUtil.readValueList(jsonStr, clazz);
            } catch (Exception ex) {
                log.error("Redis List JSON 反序列化失败，key：{}", key, ex);
                return Collections.emptyList();
            }
        }
    }

    public static <T> List<T> getList(String key, Class<T> clazz) {
        return getList(key, 0L, Integer.MAX_VALUE, clazz);
    }

    public static Long overwriteList(String key, List<Object> values) {
        if (isBlank(key) || values == null || values.isEmpty()) {
            log.warn("Redis overwriteList 参数异常：key={}, values={}", key, values);
            return -1L;
        }
        try {
            getTemplate().delete(key);
            log.debug("Redis overwriteList 清空原有数据，key={}", key);
            Long size = getTemplate().opsForList().rightPushAll(key, values);
            log.debug("Redis overwriteList 成功，key={}, 覆盖元素数={}, 列表长度={}", key, values.size(), size);
            return size;
        } catch (RedisSystemException e) {
            log.error("Redis overwriteList 失败，key={}", key, e);
            return -1L;
        }
    }

    public static void overwriteList(String key, Object value, long seconds) {
        if (value == null) {
            return;
        }
        overwriteList(key, List.of(value));
        if (seconds > 0) {
            expire(key, seconds);
        }
    }

    public static long getListSize(String key) {
        try {
            Long size = getTemplate().opsForList().size(key);
            return size == null ? 0L : size;
        } catch (Exception e) {
            log.error("getListSize 操作失败，key={}, error={}", key, e.getMessage());
            return 0L;
        }
    }

    public static void rightPush(String key, Object value) {
        getTemplate().opsForList().rightPush(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T leftPop(String key) {
        Object obj = getTemplate().opsForList().leftPop(key);
        if (obj == null) return null;
        try {
            return (T) obj;
        } catch (ClassCastException e) {
            String jsonStr = JsonNodeUtil.writeValueAsString(obj);
            return (T) JsonNodeUtil.readValue(jsonStr, obj.getClass());
        }
    }

    public static void rightPushAll(String key, List<?> values, long seconds) {
        getTemplate().opsForList().rightPushAll(key, values);
        if (seconds > 0) {
            expire(key, seconds);
        }
    }

    public static boolean updateListIndex(String key, long index, Object value) {
        try {
            getTemplate().opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("updateListIndex 操作失败，key={}, index={}, error={}", key, index, e.getMessage());
            return false;
        }
    }

    public static long removeList(String key, long count, Object value) {
        try {
            Long result = getTemplate().opsForList().remove(key, count, value);
            return result == null ? 0L : result;
        } catch (Exception e) {
            log.error("removeList 操作失败，key={}, error={}", key, e.getMessage());
            return 0L;
        }
    }

    public static void trimList(String key, long start, long end) {
        getTemplate().opsForList().trim(key, start, end);
    }

    public List<Object> multiGet(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyList();
        }
        List<Object> list = getTemplate().opsForValue().multiGet(keys);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().filter(Objects::nonNull).toList();
    }
}
