package com.cspot.insurahub.consumer;

public class IdentityProviderRegistrationException extends RuntimeException {

    public IdentityProviderRegistrationException(String message) {
        super(message);
    }

    public IdentityProviderRegistrationException(Throwable cause) {
        super(cause);
    }

    public IdentityProviderRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
