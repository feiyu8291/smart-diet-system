package com.diet.modules.common.entity;

import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "系统繁忙，请稍后再试"),
    PARAM_ERROR(400, "参数错误，请检查"),
    UNAUTHORIZED(401, "用户未登录"),
    FORBIDDEN(403, "无访问权限");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
