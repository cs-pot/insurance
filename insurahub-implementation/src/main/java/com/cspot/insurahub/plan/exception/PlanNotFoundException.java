package com.cspot.insurahub.plan.exception;

import java.util.UUID;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(UUID planId) {
        super("Plan was not found: " + planId);
    }
}
