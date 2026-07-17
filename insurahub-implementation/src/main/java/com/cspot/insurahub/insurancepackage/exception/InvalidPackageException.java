package com.cspot.insurahub.insurancepackage.exception;

public class InvalidPackageException extends RuntimeException {

    private final String code;

    public InvalidPackageException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
