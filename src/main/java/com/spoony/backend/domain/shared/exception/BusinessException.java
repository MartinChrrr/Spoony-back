package com.spoony.backend.domain.shared.exception;

public abstract class BusinessException extends RuntimeException {

    private final String code;
    private final int httpStatus;

    protected BusinessException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
