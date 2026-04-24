package com.lottery.common.exception;

import com.lottery.common.response.CommonErrorCode;

public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        this(CommonErrorCode.BAD_REQUEST.code(), message);
    }

    public BusinessException(CommonErrorCode errorCode, String message) {
        this(errorCode.code(), message);
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

