package com.diet.modules.common.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类文件描述： 数据字典转换自定义注解
 * 适用于 字段；适用于 运行时
 *
 * @author Fei_Yu
 * @date 2021/7/28 14:00
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictAnnotation {

    /**
     * 数据字典类型
     *
     * @return 参数值
     */
    DictTypeEnum type();

    /**
     * 数据字典转换值
     *
     * @return 返回文本的字段名
     */
    String target() default "";

}
