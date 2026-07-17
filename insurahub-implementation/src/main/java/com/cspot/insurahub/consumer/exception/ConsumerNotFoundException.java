package com.cspot.insurahub.consumer.exception;

public class ConsumerNotFoundException extends RuntimeException {
    public ConsumerNotFoundException(String message) {
        super(message);
    }
}
