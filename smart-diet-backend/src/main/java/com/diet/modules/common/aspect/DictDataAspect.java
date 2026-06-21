package com.diet.modules.common.aspect;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diet.modules.common.util.DictAnnotationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据字典转换切面
 * 处理返回值中的数据字典注解转换
 *
 * @author Fei_Yu
 * @date 2024/12/18
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DictDataAspect {

    @Around("@annotation(dictData)")
    public Object around(ProceedingJoinPoint point, DictData dictData) throws Throwable {
        log.info("执行数据字典转换:{}", dictData.value());
        // 执行原方法
        Object result = point.proceed();
        // 对返回结果进行数据字典转换
        if (result == null) {
            return null;
        }
        return convertDict(result);
    }

    /**
     * 根据不同类型进行数据字典转换
     *
     * @param result 返回结果
     * @return 转换后的结果
     */
    private Object convertDict(Object result) {
        if (result == null) {
            return null;
        }

        // 单独对象处理
        if (!isCollectionOrPage(result)) {
            DictAnnotationUtil.convertFieldDictOne(result);
            return result;
        }

        // List 类型处理
        if (result instanceof List) {
            DictAnnotationUtil.convertFieldDictList((List<?>) result);
            return result;
        }

        // PageInfo 类型处理
        if (result instanceof IPage<?> pageResult) {
            if (pageResult.getRecords() != null && !pageResult.getRecords().isEmpty()) {
                DictAnnotationUtil.convertFieldDictList(pageResult.getRecords());
            }
            return pageResult;
        }

        return result;
    }

    /**
     * 判断是否为集合或分页类型
     *
     * @param obj 待判断对象
     * @return 是否为集合或分页类型
     */
    private boolean isCollectionOrPage(Object obj) {
        return obj instanceof List || obj instanceof Page;
    }
}