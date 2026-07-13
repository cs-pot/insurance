package com.cspot.insurahub.consumer.exception;

public class IdentityProviderException extends RuntimeException {

    public IdentityProviderException(String message) {
        super(message);
    }

    public IdentityProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityProviderException(Throwable cause) {
        super(cause);
    }
}
