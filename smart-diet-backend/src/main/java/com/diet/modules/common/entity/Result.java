package com.diet.modules.common.entity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 统一响应封装
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Getter
@Setter
@Schema(description = "统一响应体")
public class Result<T> {

    @Schema(description = "状态码")
    private int code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "分页信息")
    private PageResult page;

    @Data
    public static class PageResult {
        private int pageNum;
        private int pageSize;
        private long total;
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private Result(int code, String message, T data, PageResult page) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.page = page;
    }

    private Result(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }

    // ===================== 成功响应 =====================

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<List<T>> successPage(IPage<T> page) {
        PageResult pageResult = new PageResult();
        pageResult.setPageNum((int) page.getCurrent());
        pageResult.setPageSize((int) page.getSize());
        pageResult.setTotal(page.getTotal());
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), page.getRecords(), pageResult);
    }

    // ===================== 失败响应 =====================

    public static <T> Result<T> failed() {
        return new Result<>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage());
    }

    public static <T> Result<T> failed(String message) {
        return new Result<>(ResultCode.FAILED.getCode(), message);
    }

    public static Result<Void> failed(int code, String message) {
        return new Result<>(code, message);
    }

    public static Result<Void> failed(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }
}
