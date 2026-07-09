package com.cspot.insurahub;

import com.cspot.insurahub.consumer.exception.EmailAlreadyInUseException;
import com.cspot.insurahub.consumer.exception.UserCreationException;
import com.cspot.insurahub.model.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
        logException(e);
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
        logException(e);
        ErrorDto errorDto = new ErrorDto()
                .error("EMAIL_ALREADY_IN_USE")
                .status(409)
                .message("The specified email is already in use.")
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
        return errorDto;
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorDto handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                          HttpServletRequest request) {
        logException(e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ErrorDto errorDto = new ErrorDto()
                .error("VALIDATION_FAILED")
                .status(400)
                .message(message)
                .timestamp(OffsetDateTime.now(clock))
                .path(request.getRequestURI());
        return errorDto;
    }

    private void logException(Exception e) {
        log.debug("Exception encountered", e);
    }
}
