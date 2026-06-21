package com.diet.modules.common.enums;

import lombok.Getter;

/**
 * 操作日志类型枚举
 *
 * @author AntiGravity
 * @date 2026-05-21
 */
@Getter
public enum SysOperationTypeEnum {

    CLUE_CREATE("1", "录入线索"),
    CLUE_CLAIM("2", "线索领取"),
    CLUE_ASSIGN("3", "线索分配"),
    CLUE_FOLLOW("4", "填写跟进"),
    CLUE_RELEASE("5", "释放公海"),
    CLUE_UPDATE("6", "修改线索"),
    DEAL_APPLY("7", "提交成交申请"),
    DEAL_APPROVE("8", "审批通过"),
    DEAL_REJECT("9", "审批驳回"),
    CONFIG_SAVE_RULES("10", "保存规则配置"),
    CONFIG_SAVE("11", "保存配置"),
    CLUE_DECRYPT("12", "数据解密");

    private final String code;
    private final String value;

    SysOperationTypeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }
}
