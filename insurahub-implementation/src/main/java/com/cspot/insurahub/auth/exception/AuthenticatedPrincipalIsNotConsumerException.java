package com.cspot.insurahub.auth.exception;

public class AuthenticatedPrincipalIsNotConsumerException extends RuntimeException {

    public AuthenticatedPrincipalIsNotConsumerException(String message) {
        super(message);
    }

    public AuthenticatedPrincipalIsNotConsumerException(Throwable cause) {
        super(cause);
    }

    public AuthenticatedPrincipalIsNotConsumerException(String message, Throwable cause) {
        super(message, cause);
    }
}
