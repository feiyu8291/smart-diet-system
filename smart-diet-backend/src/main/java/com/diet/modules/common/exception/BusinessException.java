package com.diet.modules.common.exception;

import com.diet.modules.common.entity.ResultCode;
import lombok.Getter;

/**
 * 业务自定义异常类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 全参私有构造
     */
    private BusinessException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.code = errorCode;
    }

    /**
     * 创建默认异常
     */
    public static BusinessException defaultException() {
        ResultCode defaultState = ResultCode.FAILED;
        return new BusinessException(defaultState.getMessage(), null, defaultState.getCode());
    }

    /**
     * 自定义消息异常
     */
    public static BusinessException withMessage(String message) {
        ResultCode defaultState = ResultCode.FAILED;
        return new BusinessException(message, null, defaultState.getCode());
    }

    /**
     * 参数校验异常
     */
    public static BusinessException withMessageParamsError(String message) {
        ResultCode defaultState = ResultCode.PARAM_ERROR;
        return new BusinessException(message, null, defaultState.getCode());
    }

    /**
     * 响应码枚举异常
     */
    public static BusinessException withStateEnum(ResultCode resultCode) {
        return new BusinessException(resultCode.getMessage(), null, resultCode.getCode());
    }

    /**
     * 自定义消息和状态码
     */
    public static BusinessException withMessageAndCode(String message, int errorCode) {
        return new BusinessException(message, null, errorCode);
    }

    /**
     * 自定义消息、状态码及异常根因
     */
    public static BusinessException withMessageCodeAndCause(String message, int errorCode, Throwable cause) {
        return new BusinessException(message, cause, errorCode);
    }

    /**
     * 自定义消息及异常根因
     */
    public static BusinessException withMessageAndCause(String message, Throwable cause) {
        return new BusinessException(message, cause, ResultCode.FAILED.getCode());
    }

    /**
     * 异常根因及响应码枚举
     */
    public static BusinessException withCauseAndResultCode(Throwable cause, ResultCode resultCode) {
        return new BusinessException(resultCode.getMessage(), cause, resultCode.getCode());
    }
}
