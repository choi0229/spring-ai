package com.example.loyalty.common;

public class ServiceException extends RuntimeException {

    private final ServiceExceptionCode code;

    public ServiceException(ServiceExceptionCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public ServiceException(ServiceExceptionCode code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceExceptionCode getCode() {
        return code;
    }
}
