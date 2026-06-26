package com.diet.modules.common.aspect;

import lombok.Getter;

/**
 * 类文件描述：数据字典类型
 *
 * @author Fei_Yu
 * @date 2021/6/28 14:30
 */
@Getter
public enum DictTypeEnum {

    // 资源映射类型
    RESOURCE_BASE64("资源Base64", "resource_base64", "图片地址转换", Category.RESOURCE_BASE64),

    // 系统基础字典
    GENDER_TYPE("性别类型", "gender_type", "1男 2女 0未知", Category.NORMAL),
    USE_STATUS_TYPE("是否禁用状态", "use_status_type", "0启用 1禁用", Category.NORMAL),
    MENU_TYPE("菜单类型", "menu_type", "1菜单 2按钮 3其他", Category.NORMAL),
    QUARTZ_JOB_TYPE("定时任务类型", "quartz_job_type", "1系统任务 2业务任务", Category.NORMAL),
    COMMON_FLAG("通用标识", "common_flag", "0否 1是", Category.NORMAL),

    // CRM业务相关字典
    CLUE_STATUS("线索状态", "clue_status", "0公海 1私海 2已成交", Category.NORMAL),
    APPEAL_TYPE("咨询诉求标签", "appeal_type", "咨询诉求类型", Category.NORMAL),
    CLUE_SOURCE("线索来源", "clue_source", "线索来源途径", Category.NORMAL),
    FOLLOW_INTENTION("客户意向", "follow_intention", "意向级别高/中/低/无", Category.NORMAL),
    CONTACT_TYPE("沟通方式", "contact_type", "电话/线上等", Category.NORMAL),
    PAY_METHOD("支付方式", "pay_method", "微信/支付宝/银行转账等", Category.NORMAL),
    ORDER_STATUS("售后状态", "order_status", "1跟进中 2完结", Category.NORMAL),
    SYS_OPERATION_TYPE("操作日志类型", "sys_operation_type", "登录/领取/分配/跟进等", Category.NORMAL),
    ORDER_AUDIT_STATUS("订单审核状态", "order_audit_status", "0待审核 1已通过 2已驳回", Category.NORMAL),
    FIRE_POWER_TYPE("火候类型", "fire_power_type", "0非热处理 1小火/慢炖 2中火/清蒸 3大火/爆炒", Category.NORMAL),
    CUISINE_TYPE("菜系类型", "cuisine_type", "常规菜系", Category.NORMAL),
    DIET_MODE_TYPE("膳食模式", "diet_mode_type", "0正常饮食 1轻食减脂 2放纵餐", Category.NORMAL),
    INGREDIENT_TYPE("食材类型", "ingredient_type", "1荤菜类 2素菜类 3调辅配料 4基础调味", Category.NORMAL);

    private final String dictName;
    private final String dictType;
    private final String remark;
    private final Category category;

    DictTypeEnum(String dictName, String dictType, String remark, Category category) {
        this.dictName = dictName;
        this.dictType = dictType;
        this.remark = remark;
        this.category = category;
    }

    /**
     * 字典/映射 分类
     */
    public enum Category {
        /**
         * 普通业务字典 (查 Redis/DB)
         */
        NORMAL,
        /**
         * 资源转 Base64 (查 Redis/S3)
         */
        RESOURCE_BASE64,
        /**
         * 资源转 URL (预留)
         */
        RESOURCE_URL
    }
}
