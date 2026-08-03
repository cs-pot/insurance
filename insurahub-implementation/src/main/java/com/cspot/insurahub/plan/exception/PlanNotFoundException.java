package com.cspot.insurahub.plan.exception;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException() {
        super();
    }

    public PlanNotFoundException(String message) {
        super(message);
    }
}
