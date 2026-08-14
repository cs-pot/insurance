package com.cspot.insurahub.denialreason.controller;

import com.cspot.insurahub.BaseIntegrationTest;
import com.cspot.insurahub.denialreason.entity.DenialReason;
import com.cspot.insurahub.denialreason.repository.DenialReasonRepository;
import com.cspot.insurahub.model.CreateDenialReasonRequest;
import com.cspot.insurahub.model.DenialReasonResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
class DenialReasonControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DenialReasonRepository denialReasonRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void shouldReturnSeededDenialReasons() throws Exception {
        mockMvc.perform(get("/denial-reasons")
                        .with(jwtWithPermission("view:denial-reasons")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$[*].title", hasItems(
                        "Invalid or missing receipt",
                        "Amount mismatch",
                        "Service date mismatch",
                        "Outside coverage period",
                        "Exceeds coverage limit",
                        "Duplicate claim",
                        "Other"
                )))
                .andExpect(jsonPath("$[*].description").exists());
    }

    @Test
    void shouldCreateAndPersistDenialReason() throws Exception {
        CreateDenialReasonRequest request = new CreateDenialReasonRequest()
                .title("Provider not eligible")
                .description("The provider is not eligible under the current plan.");

        String responseBody = mockMvc.perform(post("/denial-reasons")
                        .with(jwtWithPermission("create:denial-reasons"))
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value(request.getTitle()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        DenialReasonResponse response = jsonMapper.readValue(responseBody, DenialReasonResponse.class);
        DenialReason savedReason = denialReasonRepository.findById(response.getId())
                .orElseThrow(() -> new AssertionError("Created denial reason must be persisted"));

        org.assertj.core.api.Assertions.assertThat(response.getId()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(savedReason.getId()).isEqualTo(response.getId());
        org.assertj.core.api.Assertions.assertThat(savedReason.getTitle()).isEqualTo(request.getTitle());
        org.assertj.core.api.Assertions.assertThat(savedReason.getDescription()).isEqualTo(request.getDescription());
    }

    @Test
    void shouldRejectCreatingDenialReasonWithoutRequiredFields() throws Exception {
        mockMvc.perform(post("/denial-reasons")
                        .with(jwtWithPermission("create:denial-reasons"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldRequirePermissionToReadDenialReasons() throws Exception {
        mockMvc.perform(get("/denial-reasons")
                        .with(authenticatedUser()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationToReadDenialReasons() throws Exception {
        mockMvc.perform(get("/denial-reasons"))
                .andExpect(status().isUnauthorized());
    }

    private RequestPostProcessor authenticatedUser() {
        return jwt()
                .jwt(jwt -> jwt
                        .subject("auth0|test-user")
                        .claim("permissions", List.of()))
                .authorities(jwt -> Objects.requireNonNull(
                        jwtAuthenticationConverter.convert(jwt)).getAuthorities());
    }

    private RequestPostProcessor jwtWithPermission(String permission) {
        return jwt()
                .jwt(jwt -> jwt
                        .subject("auth0|admin")
                        .claim("permissions", List.of(permission)))
                .authorities(jwt -> Objects.requireNonNull(
                        jwtAuthenticationConverter.convert(jwt)).getAuthorities());
    }
}
