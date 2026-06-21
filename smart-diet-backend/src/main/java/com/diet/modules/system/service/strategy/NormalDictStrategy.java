package com.diet.modules.system.service.strategy;

import cn.hutool.core.collection.CollUtil;
import com.diet.modules.common.aspect.DictConvertStrategy;
import com.diet.modules.common.aspect.DictTypeEnum;
import com.diet.modules.system.service.SysDataDictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 普通数据字典转换策略
 *
 * @author AntiGravity
 * @date 2026-02-14
 */
@Component
@RequiredArgsConstructor
public class NormalDictStrategy implements DictConvertStrategy {

    private final SysDataDictionaryService sysDataDictionaryService;

    @Override
    public DictTypeEnum.Category getCategory() {
        return DictTypeEnum.Category.NORMAL;
    }

    @Override
    public boolean supports(DictTypeEnum type) {
        return DictTypeEnum.Category.NORMAL.equals(type.getCategory());
    }

    @Override
    public Map<Object, String> getMap(String dictType, Set<Object> codes) {
        if (CollUtil.isEmpty(codes)) {
            return new HashMap<>();
        }

        // 获取全量字典 Map (Map<String, String>)
        Map<String, String> dictMap = sysDataDictionaryService.getDictMapByType(dictType);
        if (CollUtil.isEmpty(dictMap)) {
            return new HashMap<>();
        }

        // 构造结果
        Map<Object, String> result = new HashMap<>();
        for (Object code : codes) {
            String value = getValue(dictMap, code);
            if (value != null) {
                result.put(code, value);
            }
        }
        return result;
    }

    /**
     * 安全获取字典值
     */
    private String getValue(Map<String, String> dictMap, Object code) {
        if (code == null) {
            return null;
        }
        return dictMap.get(String.valueOf(code));
    }
}
