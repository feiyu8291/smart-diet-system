package com.diet.modules.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import com.diet.modules.common.aspect.DictAnnotation;
import com.diet.modules.common.aspect.DictConvertStrategy;
import com.diet.modules.common.aspect.DictTypeEnum;
import com.diet.modules.common.config.SpringBeanHolder;
import com.diet.modules.common.constant.CharacterConstant;
import com.diet.modules.common.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 数据字典注解转换工具类（策略模式版）
 * 核心优化：引入策略模式，支持多种类型的字段转换（如字典、资源等）；支持批量处理，减少 IO 次数。
 *
 * @author Fei_Yu
 * @date 2021/7/28 14:13
 */
@Slf4j
@UtilityClass
public class DictAnnotationUtil {

    /**
     * 字典后缀
     */
    private static final String DICT_TEXT_SUFFIX = "Str";

    /**
     * 反射字段缓存(避免重复反射,提升性能)
     */
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 策略映射缓存 (Category -> Strategy)
     * 使用 EnumMap 提升查找性能,实现 O(1) 时间复杂度
     */
    private static final AtomicReference<Map<DictTypeEnum.Category, DictConvertStrategy>> STRATEGY_MAP_REF = new AtomicReference<>();

    /**
     * 获取所有转换策略映射(懒加载 + 双重检查锁)
     * 使用 EnumMap 存储策略,实现 O(1) 查找性能
     *
     * @return 策略映射表
     */
    private static Map<DictTypeEnum.Category, DictConvertStrategy> getStrategyMap() {
        Map<DictTypeEnum.Category, DictConvertStrategy> strategyMap = STRATEGY_MAP_REF.get();
        if (strategyMap != null) {
            return strategyMap;
        }
        synchronized (DictAnnotationUtil.class) {
            Map<DictTypeEnum.Category, DictConvertStrategy> strategyMapSecond = STRATEGY_MAP_REF.get();
            if (strategyMapSecond != null) {
                return strategyMapSecond;
            }
            Map<String, DictConvertStrategy> beans = SpringBeanHolder.getBeansOfType(DictConvertStrategy.class);
            if (CollUtil.isEmpty(beans)) {
                log.warn("未找到任何 DictConversionStrategy 实现类,字典转换功能可能无法正常工作");
                return new EnumMap<>(DictTypeEnum.Category.class);
            }

            // 构造 EnumMap 提升性能
            Map<DictTypeEnum.Category, DictConvertStrategy> map = new EnumMap<>(DictTypeEnum.Category.class);
            for (DictConvertStrategy strategy : beans.values()) {
                map.put(strategy.getCategory(), strategy);
            }
            STRATEGY_MAP_REF.set(map);
            log.info("字典转换策略初始化完成,共加载 {} 个策略: {}", map.size(),
                    map.keySet().stream().map(Enum::name).collect(Collectors.joining(", ")));
            return map;
        }
    }

    /**
     * 转换单个对象的字典注解属性
     * 扫描对象中所有带 @DictAnnotation 注解的字段,根据策略进行转换
     *
     * @param object 目标对象
     */
    public static void convertFieldDictOne(Object object) {
        if (Objects.isNull(object)) {
            return;
        }
        // key:字段名称，value：字段相关信息
        Map<String, Field> fieldMap = getFieldMap(object.getClass());
        if (CollUtil.isEmpty(fieldMap)) {
            return;
        }
        // 收集字段的相关信息
        List<DictFieldInfo> validDictFieldList = collectValidDictFieldInfo(object, fieldMap);
        // 执行转换逻辑
        executeStrategies(validDictFieldList);
    }

    /**
     * 转换集合对象的字典注解属性(批量优化版)
     * 核心优化: 先收集所有对象的待转换字段,再统一执行策略查询,最大限度减少 I/O 次数
     *
     * @param objectList 目标对象列表
     */
    public static void convertFieldDictList(List<?> objectList) {
        if (CollUtil.isEmpty(objectList)) {
            return;
        }

        List<DictFieldInfo> allFieldInfos = new ArrayList<>();

        // 按对象类型分组,减少重复反射
        Map<Class<?>, List<Object>> classObjectMap = objectList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(Object::getClass));

        // 收集所有对象的有效字典字段信息
        for (Map.Entry<Class<?>, List<Object>> entry : classObjectMap.entrySet()) {
            Class<?> clazz = entry.getKey();
            List<Object> objects = entry.getValue();
            Map<String, Field> fieldMap = getFieldMap(clazz);

            for (Object obj : objects) {
                allFieldInfos.addAll(collectValidDictFieldInfo(obj, fieldMap));
            }
        }

        // 统一批量执行策略(关键优化点)
        executeStrategies(allFieldInfos);
    }

    /**
     * 核心方法: 执行转换策略
     * 将字段按策略分组,然后批量执行每个策略的转换逻辑
     *
     * @param fieldInfos 待转换的字段信息列表
     */
    private static void executeStrategies(List<DictFieldInfo> fieldInfos) {
        if (CollUtil.isEmpty(fieldInfos)) {
            return;
        }
        // 获取转换代理对象 按分类映射
        Map<DictTypeEnum.Category, DictConvertStrategy> strategyTypeMap = getStrategyMap();
        // 根据字段的注解，获取对应的转换策略，按照策略将字段信息进行分组。
        Map<DictConvertStrategy, List<DictFieldInfo>> strategyMap = new HashMap<>();
        for (DictFieldInfo fieldInfo : fieldInfos) {
            DictTypeEnum dictTypeEnum = fieldInfo.getAnnotation().type();
            DictConvertStrategy strategy = strategyTypeMap.get(dictTypeEnum.getCategory());
            if (strategy != null) {
                strategyMap.computeIfAbsent(strategy, k -> new ArrayList<>()).add(fieldInfo);
            } else {
                log.warn("未找到支持类型【{}】的转换策略", dictTypeEnum.name());
            }
        }
        // 根据策略分组执行
        strategyMap.forEach(DictAnnotationUtil::processStrategy);
    }

    /**
     * 处理单个策略的转换逻辑
     * 流程: 收集代码 -> 批量查询值 -> 应用转换值
     *
     * @param strategy 转换策略
     * @param fields   该策略需要处理的字段列表
     */
    private static void processStrategy(DictConvertStrategy strategy, List<DictFieldInfo> fields) {
        // 收集代码 key：dict_type，value：字段值set集合；数据字典:data_code，geometry:geometry对象，resourceBase64:storage_id,org：org_code
        Map<String, Set<Object>> typeToCodesMap = new HashMap<>();
        for (DictFieldInfo field : fields) {
            typeToCodesMap.computeIfAbsent(field.getDataType(), k -> new HashSet<>()).add(field.getFieldValue());
        }
        // 批量查询值
        Map<String, Map<Object, String>> typeToValueMap = new HashMap<>();
        for (Map.Entry<String, Set<Object>> typeEntry : typeToCodesMap.entrySet()) {
            Map<Object, String> map = strategy.getMap(typeEntry.getKey(), typeEntry.getValue());
            if (CollUtil.isNotEmpty(map)) {
                typeToValueMap.put(typeEntry.getKey(), map);
            }
        }
        // 应用转换值  支持多目标字段转换 (如 Geometry -> longitude,latitude)
        for (DictFieldInfo field : fields) {
            Map<Object, String> valueMap = typeToValueMap.get(field.getDataType());
            Object fieldValue = field.getFieldValue();
            if (Objects.isNull(valueMap) || CharSequenceUtil.isBlank(valueMap.get(fieldValue))) {
                continue;
            }
            applyFieldValue(field, valueMap.get(fieldValue));
        }
    }

    /**
     * 将转换后的文本值应用到目标对象的对应字段中
     * 支持单字段及以逗号分隔的多字段赋值映射 (如 Geometry -> longitude,latitude)
     *
     * @param field 字段信息
     * @param text  待赋值的文本
     */
    private static void applyFieldValue(DictFieldInfo field, String text) {
        String targetName = field.getTargetName();

        // 单字段赋值场景
        if (!targetName.contains(CharacterConstant.COMMA)) {
            setFieldValue(field.getTargetObj(), targetName, text);
            return;
        }

        // 多字段赋值场景
        String[] targets = targetName.split(CharacterConstant.COMMA);
        String[] values = text.split(CharacterConstant.COMMA);

        int length = Math.min(targets.length, values.length);
        for (int i = 0; i < length; i++) {
            setFieldValue(field.getTargetObj(), targets[i].trim(), values[i].trim());
        }
    }

    /**
     * 重载方法：默认忽略空值,返回目标对象
     */
    public static <T> T copyPropertiesAndDict(Object source, Class<T> targetClass) {
        try {
            T targetObj = targetClass.getDeclaredConstructor().newInstance();
            copyPropertiesAndDict(source, targetObj, true);
            return targetObj;
        } catch (Exception e) {
            log.error("复制属性并转换字典失败", e);
            throw BusinessException.withMessageAndCause("属性复制失败", e);
        }
    }

    /**
     * 重载方法：默认忽略空值
     */
    public static void copyPropertiesAndDict(Object sourceObj, Object targetObj) {
        copyPropertiesAndDict(sourceObj, targetObj, true);
    }

    /**
     * 复制属性并转换数据字典
     */
    public static void copyPropertiesAndDict(Object sourceObj, Object targetObj, boolean ignoreNull) {
        Assert.notNull(sourceObj, "源对象不能为空");
        Assert.notNull(targetObj, "目标对象不能为空");
        try {
            BeanUtil.copyProperties(sourceObj, targetObj,
                    CopyOptions.create().setIgnoreNullValue(ignoreNull).setIgnoreError(true).setIgnoreCase(false));
            convertFieldDictOne(targetObj);
        } catch (Exception e) {
            log.error("复制属性并转换字典失败", e);
            throw BusinessException.withMessageAndCause("属性复制失败", e);
        }
    }


    // ------------------------------ 私有结构化工具方法 ------------------------------

    /**
     * @param object   转换对象（单个对象，list，page对象）
     * @param fieldMap 对象的字段信息map
     * @return java.util.List<com.hhxx.combat.modules.common.util.DictAnnotationUtil.DictFieldInfo>
     * @author Mr_Fei
     * @date 2026/3/23 14:00
     * @description 收集所有有效字典字段信息
     */
    private static List<DictFieldInfo> collectValidDictFieldInfo(Object object, Map<String, Field> fieldMap) {
        List<DictFieldInfo> dictFieldList = new ArrayList<>();
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            String fieldName = entry.getKey();
            Field field = entry.getValue();

            // 1. 构建字段信息对象，统一存储所有属性
            DictFieldInfo fieldInfo = buildDictFieldInfo(object, fieldName, field);

            // 2. 统一校验字段信息有效性（单个if判断，无continue）
            if (fieldInfo.isValid()) {
                dictFieldList.add(fieldInfo);
            }
        }
        return dictFieldList;
    }

    /**
     * 构建字典字段信息
     */
    private static DictFieldInfo buildDictFieldInfo(Object object, String fieldName, Field field) {
        DictFieldInfo fieldInfo = new DictFieldInfo();
        // 设置目标对象引用
        fieldInfo.setTargetObj(object);
        fieldInfo.setFieldName(fieldName);
        // 1. 检查注解是否存在
        DictAnnotation annotation = field.getAnnotation(DictAnnotation.class);
        if (Objects.isNull(annotation)) {
            fieldInfo.setValid(false);
            return fieldInfo;
        }
        fieldInfo.setAnnotation(annotation);
        // 2. 获取字段值
        Object fieldValue = ReflectUtil.getFieldValue(object, field);
        // 有注解，但该字段没有值，也不进行转换
        if (Objects.isNull(fieldValue)) {
            fieldInfo.setValid(false);
            return fieldInfo;
        }
        // 字段值
        fieldInfo.setFieldValue(fieldValue);
        // 数据字典类型 dict_type
        String dataType = annotation.type().getDictType();
        fieldInfo.setDataType(dataType);
        // 计算目标字段值
        String targetName = CharSequenceUtil.isBlank(annotation.target()) ? fieldName + DICT_TEXT_SUFFIX : annotation.target();
        fieldInfo.setTargetName(targetName);
        // 字段有效（待转换）
        fieldInfo.setValid(true);
        return fieldInfo;
    }

    /**
     * 获取类的字段映射（带缓存）
     */
    private static Map<String, Field> getFieldMap(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, key -> {
            Field[] fields = ReflectUtil.getFields(clazz);
            Map<String, Field> fieldMap = new HashMap<>(fields.length);
            for (Field field : fields) {
                fieldMap.put(field.getName(), field);
            }
            return fieldMap;
        });
    }

    /**
     * 安全设置字段值
     * 优化：重新获取 Field 对象以确保准确性，或者使用缓存的 Field
     * 这里为了简化，我们直接反射设置。由于 DictFieldInfo 中没有保存 Field 对象引用，
     * 但我们有 TargetObj 和 TargetName (fieldName)。
     * 为了性能，getFieldMap 其实已经缓存了 Field。
     */
    private static void setFieldValue(Object targetObj, String targetFieldName, Object value) {
        try {
            // 获取目标字段
            Field field = getFieldMap(targetObj.getClass()).get(targetFieldName);
            if (field == null) {
                log.warn("目标字段【{}】不存在于类【{}】中", targetFieldName, targetObj.getClass().getSimpleName());
                return;
            }
            // 安全转换类型并设置
            Object convertedValue = Convert.convert(field.getType(), value);
            ReflectUtil.setFieldValue(targetObj, field, convertedValue);
        } catch (Exception e) {
            log.error("设置字段【{}】值【{}】失败", targetFieldName, value, e);
        }
    }

    // ------------------------------ 内部辅助类 ------------------------------

    @Getter
    @Setter
    private static class DictFieldInfo {

        /**
         * 目标对象引用，整个对象，包含所有字段及目标字段信息
         */
        private Object targetObj;
        /**
         * 原始字段名称
         */
        private String fieldName;
        /**
         * 注解信息
         */
        private DictAnnotation annotation;
        /**
         * 转换前原始值
         */
        private Object fieldValue;
        /**
         * 数据字典类型
         *
         * @see DictTypeEnum dict_type
         */
        private String dataType;
        /**
         * 目标字段名 默认使用 fieldName+Str
         */
        private String targetName;
        /**
         * true:有转换注解，false：无转换注解
         */
        private boolean valid;
    }

}