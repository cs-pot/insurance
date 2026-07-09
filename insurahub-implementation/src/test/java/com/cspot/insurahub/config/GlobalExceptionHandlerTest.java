package com.cspot.insurahub.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestErrorController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void missingRequestBodyIsFormattedWithoutDetails() throws Exception {
        ResultActions result = mockMvc.perform(post("/error/body")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertError(result, "REQUEST_BODY_INVALID", "Request body is missing or invalid");
    }

    @Test
    void malformedRequestBodyIsFormattedWithoutDetails() throws Exception {
        ResultActions result = mockMvc.perform(post("/error/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());

        assertError(result, "REQUEST_BODY_INVALID", "Request body is missing or invalid");
    }

    @Test
    void validationErrorIncludesFieldErrors() throws Exception {
        mockMvc.perform(post("/error/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("must not be blank"));
    }

    @Test
    void unexpectedExceptionIsLoggedAndFormattedWithoutDetails() throws Exception {
        ResultActions result = mockMvc.perform(get("/error/unexpected"))
                .andExpect(status().isInternalServerError());

        assertError(result, "INTERNAL_SERVER_ERROR", "Unexpected error occurred");
    }

    private void assertError(
            ResultActions result,
            String code,
            String message
    ) throws Exception {
        result.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}"));
    }

    private record TestRequest(@NotBlank String name) {
    }

    @RestController
    private static class TestErrorController {

        @PostMapping(path = "/error/body", consumes = MediaType.APPLICATION_JSON_VALUE)
        ResponseEntity<Void> readBody(@Valid @RequestBody TestRequest request) {
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/error/unexpected")
        ResponseEntity<Void> failUnexpectedly() {
            throw new IllegalStateException("SQL detail must not leak");
        }
    }
}
