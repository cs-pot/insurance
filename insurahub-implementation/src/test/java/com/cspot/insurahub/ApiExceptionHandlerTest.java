package com.cspot.insurahub;

import com.cspot.insurahub.claim.exception.InvalidReceiptException;
import com.cspot.insurahub.common.exception.ResourceNotFoundException;
import com.cspot.insurahub.model.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-09T12:00:00Z"),
            ZoneOffset.UTC
    );

    private ApiExceptionHandler handler;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler(CLOCK);
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/claims");
    }

    @Test
    void shouldHandleResourceNotFoundException() throws NoSuchMethodException {
        ResourceNotFoundException exception = new ResourceNotFoundException(
                "Receipt not found with id: receipt-id"
        );

        ErrorDto response = handler.handleResourceNotFoundException(exception, request);

        assertThat(response.getError()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo(exception.getMessage());
        assertThat(response.getTimestamp()).isEqualTo(OffsetDateTime.now(CLOCK));
        assertThat(response.getPath()).isEqualTo("/api/v1/claims");
        assertHandlerStatus(
                "handleResourceNotFoundException",
                ResourceNotFoundException.class,
                HttpStatus.NOT_FOUND
        );
    }

    @Test
    void shouldHandleInvalidReceiptException() throws NoSuchMethodException {
        InvalidReceiptException exception = new InvalidReceiptException(
                "Receipt must be a PDF, JPG, or PNG file"
        );

        ErrorDto response = handler.handleInvalidReceiptException(exception, request);

        assertThat(response.getError()).isEqualTo("INVALID_RECEIPT");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo(exception.getMessage());
        assertThat(response.getTimestamp()).isEqualTo(OffsetDateTime.now(CLOCK));
        assertThat(response.getPath()).isEqualTo("/api/v1/claims");
        assertHandlerStatus(
                "handleInvalidReceiptException",
                InvalidReceiptException.class,
                HttpStatus.BAD_REQUEST
        );
    }

    private void assertHandlerStatus(
            String methodName,
            Class<?> exceptionType,
            HttpStatus expectedStatus
    ) throws NoSuchMethodException {
        Method method = ApiExceptionHandler.class.getMethod(
                methodName,
                exceptionType,
                HttpServletRequest.class
        );
        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(
                method,
                ResponseStatus.class
        );

        assertThat(responseStatus).isNotNull();
        assertThat(responseStatus.code()).isEqualTo(expectedStatus);
    }
}
