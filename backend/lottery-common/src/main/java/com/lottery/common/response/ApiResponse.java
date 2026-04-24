package com.lottery.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String code, String message, T data, long timestamp) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(CommonErrorCode.SUCCESS.code(), CommonErrorCode.SUCCESS.message(), data, System.currentTimeMillis());
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static ApiResponse<Void> failure(CommonErrorCode errorCode) {
        return new ApiResponse<>(errorCode.code(), errorCode.message(), null, System.currentTimeMillis());
    }

    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<>(code, message, null, System.currentTimeMillis());
    }
}

