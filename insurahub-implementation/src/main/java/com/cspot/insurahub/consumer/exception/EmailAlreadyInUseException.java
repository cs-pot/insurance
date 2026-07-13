package com.cspot.insurahub.consumer.exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String message) {
        super(message);
    }

    public EmailAlreadyInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailAlreadyInUseException(Throwable cause) {
        super(cause);
    }
}
