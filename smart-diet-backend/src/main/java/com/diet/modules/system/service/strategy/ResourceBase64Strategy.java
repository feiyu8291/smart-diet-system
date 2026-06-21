package com.diet.modules.system.service.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.diet.modules.common.aspect.DictConvertStrategy;
import com.diet.modules.common.aspect.DictTypeEnum;
import com.diet.modules.common.constant.CacheKeyConstant;
import com.diet.modules.common.util.RedisUtil;
import com.diet.modules.system.service.SysFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 资源转 Base64 转换策略 (带 Redis 缓存)
 *
 * @author AntiGravity
 * @date 2026-02-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceBase64Strategy implements DictConvertStrategy {

    private final SysFileStorageService sysFileStorageService;

    @Override
    public DictTypeEnum.Category getCategory() {
        return DictTypeEnum.Category.RESOURCE_BASE64;
    }

    @Override
    public boolean supports(DictTypeEnum type) {
        return DictTypeEnum.Category.RESOURCE_BASE64.equals(type.getCategory());
    }

    @Override
    public Map<Object, String> getMap(String dictType, Set<Object> codes) {
        if (CollUtil.isEmpty(codes)) {
            return new HashMap<>();
        }

        List<Object> codeList = new ArrayList<>(codes);
        Map<Object, String> result = new HashMap<>();

        // 1. 批量查询缓存
        fetchCachedValues(codeList, result);

        // 2. 处理缓存未命中的数据
        List<Object> missingCodes = codeList.stream().filter(code -> !result.containsKey(code)).toList();

        if (CollUtil.isNotEmpty(missingCodes)) {
            handleCacheMisses(missingCodes, result);
        }

        return result;
    }

    /**
     * 从 Redis 批量获取缓存值
     */
    private void fetchCachedValues(List<Object> codeList, Map<Object, String> result) {
        List<String> keys = codeList.stream()
                .map(code -> CacheKeyConstant.RESOURCE_BASE64 + code)
                .toList();

        List<Object> values = RedisUtil.multiGet(keys);
        if (CollUtil.isEmpty(values)) {
            return;
        }

        for (int i = 0; i < codeList.size() && i < values.size(); i++) {
            Object value = values.get(i);
            if (value != null && CharSequenceUtil.isNotBlank(String.valueOf(value))) {
                result.put(codeList.get(i), String.valueOf(value));
            }
        }
    }

    /**
     * 处理缓存未命中：查询 S3 并回填 Redis
     */
    private void handleCacheMisses(List<Object> missingCodes, Map<Object, String> result) {
        for (Object code : missingCodes) {
            if (Objects.isNull(code)) {
                continue;
            }
            String base64 = fetchFromS3AndCache(Long.parseLong(code.toString()));
            if (CharSequenceUtil.isNotBlank(base64)) {
                result.put(code, base64);
            }
        }
    }

    /**
     * 从 S3 获取并缓存
     */
    private String fetchFromS3AndCache(Long storageId) {
        String cacheKey = CacheKeyConstant.RESOURCE_BASE64 + storageId;
        try {
            String fullBase64 = sysFileStorageService.getFilePathById(storageId);
            if (CharSequenceUtil.isNotBlank(fullBase64)) {
                RedisUtil.set(cacheKey, fullBase64, CacheKeyConstant.REDIS_KEY_EXPIRE_2_HOUR);
                return fullBase64;
            }
        } catch (Exception e) {
            log.error("获取文件 Base64 失败, storageId: {}", storageId, e);
        }
        return null;
    }
}
