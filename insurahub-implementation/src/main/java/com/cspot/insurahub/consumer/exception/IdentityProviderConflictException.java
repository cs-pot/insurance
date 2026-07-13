package com.cspot.insurahub.consumer.exception;

public class IdentityProviderConflictException extends IdentityProviderException {

    public IdentityProviderConflictException(String message) {
        super(message);
    }

    public IdentityProviderConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityProviderConflictException(Throwable cause) {
        super(cause);
    }
}
