package com.diet.modules.common.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.diet.modules.common.constant.FieldConstant;
import com.diet.modules.common.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        if (metaObject.hasSetter(FieldConstant.CREATE_TIME)) {
            metaObject.setValue(FieldConstant.CREATE_TIME, now);
        }
        if (metaObject.hasSetter(FieldConstant.UPDATE_TIME)) {
            metaObject.setValue(FieldConstant.UPDATE_TIME, now);
        }
        String username = getCurrentUsername();
        if (metaObject.hasSetter(FieldConstant.CREATE_BY)) {
            metaObject.setValue(FieldConstant.CREATE_BY, username);
        }
        if (metaObject.hasSetter(FieldConstant.UPDATE_BY)) {
            metaObject.setValue(FieldConstant.UPDATE_BY, username);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("执行更新字段自动填充");
        LocalDateTime now = LocalDateTime.now();
        if (metaObject.hasSetter(FieldConstant.UPDATE_TIME)) {
            metaObject.setValue(FieldConstant.UPDATE_TIME, now);
        }
        String username = getCurrentUsername();
        if (metaObject.hasSetter(FieldConstant.UPDATE_BY)) {
            metaObject.setValue(FieldConstant.UPDATE_BY, username);
        }
    }

    /**
     * 获取当前用户名，若获取失败则默认返回 System
     */
    private String getCurrentUsername() {
        try {
            return SecurityUtils.getCurrentUser().getRealName();
        } catch (Exception e) {
            log.debug("无法获取当前在线用户: {}，审计字段将采用默认值：System", e.getMessage());
            return "System";
        }
    }
}
