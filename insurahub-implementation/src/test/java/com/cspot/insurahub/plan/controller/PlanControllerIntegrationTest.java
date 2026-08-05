package com.cspot.insurahub.plan.controller;

import com.cspot.insurahub.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
class PlanControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void getPlanByIdShouldReturnNotFoundForNonExistentPlan() throws Exception {
        UUID nonExistentPlanId = UUID.randomUUID();

        mockMvc.perform(get("/plans/{id}", nonExistentPlanId)
                        .with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getPlanByIdShouldReturnPlanDetails() throws Exception {
        UUID planId = seedPlan("INITIALIZED");

        mockMvc.perform(get("/plans/{id}", planId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Plan"))
                .andExpect(jsonPath("$.type").value("HEALTH_INSURANCE"));
    }

    @Test
    void getPlansShouldReturnOnlyPlansFromInitializedPackages() throws Exception {
        String initializedPlanName = "Init Plan " + randomLetters();
        String notStartedPlanName = "Not Started Plan " + randomLetters();
        seedPlan("INITIALIZED", initializedPlanName);
        seedPlan("NOT_STARTED", notStartedPlanName);

        mockMvc.perform(get("/plans")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem(initializedPlanName)))
                .andExpect(jsonPath("$[*].name", not(hasItem(notStartedPlanName))));
    }

    private RequestPostProcessor authenticatedUser() {
        return jwt()
                .jwt(jwt -> jwt
                        .subject("auth0|test-user")
                        .claim("permissions", List.of()))
                .authorities(jwt -> Objects.requireNonNull(
                        jwtAuthenticationConverter.convert(jwt)).getAuthorities());
    }

    private String randomLetters() {
        UUID uuid = UUID.randomUUID();
        StringBuilder letters = new StringBuilder();
        for (char c : uuid.toString().replace("-", "").toCharArray()) {
            if (Character.isLetter(c)) {
                letters.append(c);
            }
        }
        return letters.toString();
    }

    private UUID seedPlan(String packageStatus) {
        return seedPlan(packageStatus, "Test Plan");
    }

    private UUID seedPlan(String packageStatus, String planName) {
        UUID packageId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        String packageSql = "INSERT INTO packages (id, version, name, payroll, start_date, " +
                "end_date, status, created_at, created_by, deleted_at) " +
                "VALUES (?, 0, 'Test Package', 'MONTHLY', '2024-01-01', '2025-01-01', " +
                "?, NOW(), 'test', NULL)";
        jdbcTemplate.update(packageSql, packageId, packageStatus);

        String planSql = "INSERT INTO plans (id, version, package_id, name, type, " +
                "contribution, election, created_at, created_by, deleted_at) " +
                "VALUES (?, 0, ?, ?, 'HEALTH_INSURANCE', 100.00, 50.00, " +
                "NOW(), 'test', NULL)";
        jdbcTemplate.update(planSql, planId, packageId, planName);

        return planId;
    }
}
