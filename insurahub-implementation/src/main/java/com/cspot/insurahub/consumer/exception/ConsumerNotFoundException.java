package com.cspot.insurahub.consumer.exception;

public class ConsumerNotFoundException extends RuntimeException {
    public ConsumerNotFoundException() {
    }

    public ConsumerNotFoundException(String message) {
        super(message);
    }

    public ConsumerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsumerNotFoundException(Throwable cause) {
        super(cause);
    }
}
