package com.diet.modules.common.enums;

import lombok.Getter;

/**
 * 权限控制核心枚举
 *
 * @author Fei_Yu
 * @date 2026/05/21
 */
@Getter
public enum PermissionEnum {

    /**
     * 全局数据查看与审批权限 (超管/部门经理)
     */
    SYS_DATA_ALL("sys:data:all", "全局数据查看与审批权限"),

    /**
     * 销售线索认领权限 (普通销售/部门经理)
     */
    CLUE_CLAIM("clue:claim", "销售线索认领权限"),

    /**
     * 售后服务与维护权限 (售后人员)
     */
    CLUE_AFTERMARKET("clue:aftermarket", "售后服务与维护权限");

    private final String code;
    private final String description;

    PermissionEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
