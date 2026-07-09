package com.cspot.insurahub.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<FieldError> fieldErrors
) {
    public ErrorResponse(String code, String message) {
        this(code, message, List.of());
    }

    public record FieldError(
            String field,
            String message
    ) {
    }
}
