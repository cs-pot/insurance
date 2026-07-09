package com.cspot.insurahub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        List<ErrorResponse.FieldError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        validationMessage(error.getDefaultMessage())
                ))
                .toList();

        return format(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Request validation failed",
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception
    ) {
        return format(
                HttpStatus.BAD_REQUEST,
                "REQUEST_BODY_INVALID",
                "Request body is missing or invalid"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        LOGGER.error("Unhandled exception", exception);

        return format(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Unexpected error occurred"
        );
    }

    private ResponseEntity<ErrorResponse> format(
            HttpStatusCode status,
            String code,
            String message
    ) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message));
    }

    private ResponseEntity<ErrorResponse> format(
            HttpStatusCode status,
            String code,
            String message,
            List<ErrorResponse.FieldError> fieldErrors
    ) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, fieldErrors));
    }

    private String validationMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Invalid value";
        }

        return message;
    }
}
