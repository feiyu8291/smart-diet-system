package com.diet.modules.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Spring Bean 获取容器工具
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Component
@Slf4j
@SuppressWarnings({"unchecked", "all"})
public class SpringBeanHolder implements DisposableBean {

    private static final AtomicReference<ApplicationContext> applicationContextCache = new AtomicReference<>();
    private static final List<CallBack> CALL_BACKS = new ArrayList<>();
    private static boolean addCallback = true;

    /**
     * 针对某些初始化方法，在 SpringContextHolder 未初始化时提交回调方法。
     */
    public synchronized static void addCallBacks(CallBack callBack) {
        if (addCallback) {
            SpringBeanHolder.CALL_BACKS.add(callBack);
        } else {
            log.warn("CallBack：{} 已无法添加！立即执行", callBack.getCallBackName());
            callBack.executor();
        }
    }

    /**
     * 从静态变量 applicationContext 中取得 Bean
     */
    public static <T> T getBean(String name) {
        assertContextInjected();
        return (T) applicationContextCache.get().getBean(name);
    }

    /**
     * 从静态变量 applicationContext 中取得 Bean
     */
    public static <T> T getBean(Class<T> requiredType) {
        assertContextInjected();
        return applicationContextCache.get().getBean(requiredType);
    }

    /**
     * 获取 bean 实例，优先获取单例，单例不存在则获取原型
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        assertContextInjected();
        T bean = getBean(beanName);
        if (Objects.nonNull(bean)) {
            return bean;
        }
        return getBean(clazz);
    }

    /**
     * 从静态变量 applicationContext 中取得 Bean 集合
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        assertContextInjected();
        return applicationContextCache.get().getBeansOfType(type);
    }

    /**
     * 获取 SpringBoot 配置信息
     */
    public static <T> T getProperties(String property, T defaultValue, Class<T> requiredType) {
        T result = defaultValue;
        try {
            result = getBean(Environment.class).getProperty(property, requiredType);
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * 获取 SpringBoot 配置信息
     */
    public static String getProperties(String property) {
        return getProperties(property, null, String.class);
    }

    /**
     * 获取 SpringBoot 配置信息
     */
    public static <T> T getProperties(String property, Class<T> requiredType) {
        return getProperties(property, null, requiredType);
    }

    /**
     * 检查 ApplicationContext 不为空
     */
    private static void assertContextInjected() {
        if (applicationContextCache.get() == null) {
            throw new IllegalStateException("applicationContext属性未注入, 请在启动类或配置中注册SpringBeanHolder。");
        }
    }

    private static void clearHolder() {
        log.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContextCache.get());
        applicationContextCache.set(null);
    }

    @Override
    public void destroy() {
        SpringBeanHolder.clearHolder();
    }

    @Autowired
    public void setApplicationContext(@Lazy ApplicationContext applicationContext) throws BeansException {
        ApplicationContext cachedContext = applicationContextCache.get();
        if (cachedContext != null) {
            log.warn("SpringContextHolder中的ApplicationContext被覆盖, 原有ApplicationContext为: {}", cachedContext);
            return;
        }
        synchronized (SpringBeanHolder.class) {
            ApplicationContext secondContext = applicationContextCache.get();
            if (secondContext == null) {
                applicationContextCache.set(applicationContext);
                if (addCallback) {
                    for (CallBack callBack : SpringBeanHolder.CALL_BACKS) {
                        callBack.executor();
                    }
                    CALL_BACKS.clear();
                }
                SpringBeanHolder.addCallback = false;
            } else {
                log.warn("SpringContextHolder中的ApplicationContext被覆盖, 原有ApplicationContext为: {}", secondContext);
            }
        }
    }

    public static List<String> getAllServiceBeanName() {
        assertContextInjected();
        return new ArrayList<>(Arrays.asList(applicationContextCache.get().getBeanNamesForAnnotation(Service.class)));
    }

    public interface CallBack {
        void executor();

        default String getCallBackName() {
            return Thread.currentThread().getId() + ":" + this.getClass().getName();
        }
    }
}
