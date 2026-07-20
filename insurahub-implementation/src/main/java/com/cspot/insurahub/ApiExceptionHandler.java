package com.cspot.insurahub;

import com.cspot.insurahub.consumer.exception.EmailAlreadyInUseException;
import com.cspot.insurahub.consumer.exception.InvalidConsumerPageRequestException;
import com.cspot.insurahub.consumer.exception.ConsumerNotFoundException;
import com.cspot.insurahub.consumer.exception.UserCreationException;
import com.cspot.insurahub.insurancepackage.exception.InvalidPackageException;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.model.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ApiExceptionHandler {

    private final Clock clock;

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleUserCreationException(UserCreationException e, HttpServletRequest request) {
        logError(e);
        ErrorDto errorDto = new ErrorDto()
                .error("USER_CREATION_FAILED")
                .status(500)
                .message("Failed to create user. Please try again later.")
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
        return errorDto;
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ErrorDto handleEmailAlreadyInUseException(EmailAlreadyInUseException e, HttpServletRequest request) {
        logWarn(e);
        ErrorDto errorDto = new ErrorDto()
                .error("EMAIL_ALREADY_IN_USE")
                .status(409)
                .message("The specified email is already in use.")
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
        return errorDto;
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ErrorDto handleConsumerNotFoundException(ConsumerNotFoundException e, HttpServletRequest request) {
        logWarn(e);
        return new ErrorDto()
                .error("CONSUMER_NOT_FOUND")
                .status(404)
                .message(e.getMessage())
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorDto handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                          HttpServletRequest request) {
        logWarn(e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField())
                .collect(Collectors.joining("; "));
        ErrorDto errorDto = new ErrorDto()
                .error("VALIDATION_FAILED")
                .status(400)
                .message(message)
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
        return errorDto;
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorDto handleConstraintViolationException(ConstraintViolationException e,
                                                       HttpServletRequest request) {
        logWarn(e);
        String message = e.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String constraintName = violation.getConstraintDescriptor()
                            .getAnnotation()
                            .annotationType()
                            .getSimpleName();
                    Object value = violation.getConstraintDescriptor().getAttributes().get("value");

                    if (propertyPath.endsWith("arg0") && "Min".equals(constraintName)) {
                        return "page must be greater than or equal to " + value;
                    }
                    if (propertyPath.endsWith("arg1") && "Min".equals(constraintName)) {
                        return "size must be greater than or equal to " + value;
                    }
                    if (propertyPath.endsWith("arg1") && "Max".equals(constraintName)) {
                        return "size must be less than or equal to " + value;
                    }
                    return propertyPath + ": " + violation.getMessage();
                })
                .collect(Collectors.joining("; "));
        ErrorDto errorDto = new ErrorDto()
                .error("VALIDATION_FAILED")
                .status(400)
                .message(message)
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
        return errorDto;
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorDto handleInvalidConsumerPageRequestException(InvalidConsumerPageRequestException e,
                                                              HttpServletRequest request) {
        logWarn(e);
        ErrorDto errorDto = new ErrorDto()
                .error("VALIDATION_FAILED")
                .status(400)
                .message(e.getMessage())
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
        return errorDto;
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorDto handleMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        logWarn(e);
        ErrorDto errorDto = new ErrorDto()
                .error("MALFORMED_REQUEST_BODY")
                .status(400)
                .message("Missing or invalid request body")
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
        return errorDto;
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public ErrorDto handleAccessDeniedException(AuthorizationDeniedException e, HttpServletRequest request) {
        logWarn(e);
        ErrorDto errorDto = new ErrorDto()
                .error("ACCESS_DENIED")
                .status(403)
                .message("You do not have permissions to perform this operation.")
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
        return errorDto;
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto defaultHandler(Exception e, HttpServletRequest request) {
        logError(e);
        return new ErrorDto()
                .error("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error")
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
    }


    @ExceptionHandler(InvalidPackageException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorDto handleInvalidPackageException(
            InvalidPackageException e,
            HttpServletRequest request
    ) {
        logWarn(e);

        return new ErrorDto()
                .error(e.getCode())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message(e.getMessage())
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
    }

    @ExceptionHandler(PackageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto handlePackageNotFoundException(
            PackageNotFoundException e,
            HttpServletRequest request
    ) {
        logWarn(e);

        return new ErrorDto()
                .error("PACKAGE_NOT_FOUND")
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorDto handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException e,
            HttpServletRequest request
    ) {
        logWarn(e);

        return new ErrorDto()
                .error("UNSUPPORTED_MEDIA_TYPE")
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .message(e.getMessage())
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
    }

    private void logError(Exception e) {
        log.error("Exception encountered: ", e.getClass().getSimpleName());
        log.debug("", e);
    }

    private void logWarn(Exception e) {
        log.warn("Exception encountered: ", e.getClass().getSimpleName());
        log.debug("", e);
    }
}
