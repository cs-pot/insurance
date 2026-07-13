package com.cspot.insurahub.consumer.exception;

public class IdentityProviderRegistrationException extends IdentityProviderException {

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
