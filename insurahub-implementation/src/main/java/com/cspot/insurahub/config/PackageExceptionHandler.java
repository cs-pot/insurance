package com.cspot.insurahub.config;

import com.cspot.insurahub.insurancepackage.InvalidPackageException;
import com.cspot.insurahub.insurancepackage.PackageController;
import com.cspot.insurahub.model.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.OffsetDateTime;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = PackageController.class)
@RequiredArgsConstructor
public class PackageExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(InvalidPackageException.class)
    public ResponseEntity<ErrorDto> handleInvalidPackageException(
            InvalidPackageException exception,
            HttpServletRequest request
    ) {
        ErrorDto response = errorDto(exception.getCode(), HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(),
                request);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        ErrorDto response = errorDto("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "Request validation failed",
                request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        ErrorDto response;

        if (exception.getMessage() != null
                && exception.getMessage().contains("Required request body is missing")) {
            response = errorDto("PACKAGE_REQUEST_BODY_REQUIRED", HttpStatus.BAD_REQUEST, "Request body is required",
                    request);
        } else {
            response = errorDto("REQUEST_BODY_INVALID", HttpStatus.BAD_REQUEST, "Request body is invalid", request);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private ErrorDto errorDto(String error, HttpStatus status, String message, HttpServletRequest request) {
        return new ErrorDto()
                .error(error)
                .status(status.value())
                .message(message)
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
    }
}
