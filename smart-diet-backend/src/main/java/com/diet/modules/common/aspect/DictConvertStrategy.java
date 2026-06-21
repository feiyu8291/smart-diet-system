package com.diet.modules.common.aspect;


import java.util.Map;
import java.util.Set;

/**
 * 数据字典/映射转换策略接口
 *
 * @author AntiGravity
 * @date 2026-02-14
 */
public interface DictConvertStrategy {

    /**
     * 获取策略对应的字典分类
     *
     * @return Category
     */
    DictTypeEnum.Category getCategory();

    /**
     * 是否支持该类型的字典转换
     *
     * @param type 字典类型
     * @return boolean
     */
    boolean supports(DictTypeEnum type);

    /**
     * 批量获取字典映射值
     *
     * @param dictType 字典类型字符串（对于资源类型，可能用不到此参数，但保留接口一致性）
     * @param codes    待转换的代码/ID集合
     * @return Map<Code, Text/Value>
     */
    Map<Object, String> getMap(String dictType, Set<Object> codes);
}
