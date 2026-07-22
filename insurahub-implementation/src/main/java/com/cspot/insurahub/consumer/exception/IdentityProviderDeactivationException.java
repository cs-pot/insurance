package com.cspot.insurahub.consumer.exception;

public class IdentityProviderDeactivationException extends RuntimeException {
    public IdentityProviderDeactivationException(String message) {
        super(message);
    }

    public IdentityProviderDeactivationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityProviderDeactivationException(Throwable cause) {
        super(cause);
    }
}
