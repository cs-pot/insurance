package com.cspot.insurahub.plan.exception;

public class InvalidPlanException extends RuntimeException {

    private final String code;

    public InvalidPlanException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
