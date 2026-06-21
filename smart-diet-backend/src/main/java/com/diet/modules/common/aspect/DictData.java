package com.diet.modules.common.aspect;

import java.lang.annotation.*;

/**
 * 数据字典转换注解
 * 用于标识需要进行数据字典转换的方法
 * 支持单独对象、List和PageInfo类型的返回值转换
 *
 * @author Fei_Yu
 * @date 2024/12/18
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictData {

    /**
     * 描述信息
     *
     * @return 描述
     */
    String value() default "";
}