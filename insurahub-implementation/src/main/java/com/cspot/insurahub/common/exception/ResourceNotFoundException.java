package com.cspot.insurahub.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Class<?> resourceClass, Object id) {
        super(resourceClass.getSimpleName() + " with id '" + id + "' was not found.");
    }
}
