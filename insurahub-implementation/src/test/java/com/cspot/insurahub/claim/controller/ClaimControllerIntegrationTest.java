package com.cspot.insurahub.claim.controller;

import com.cspot.insurahub.BaseIntegrationTest;
import jakarta.persistence.EntityManager;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
class ClaimControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void denyClaimShouldReturnNoContentForPendingClaim() throws Exception {
        UUID claimId = seedClaim("PENDING");

        mockMvc.perform(put("/claims/{claimId}/deny", claimId)
                        .with(adminUser()))
                .andExpect(status().isNoContent());

        entityManager.flush();
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM claims WHERE id = ?", String.class, claimId);
        org.assertj.core.api.Assertions.assertThat(status).isEqualTo("DENIED");
    }

    @Test
    void denyClaimShouldReturnUnprocessableEntityForAlreadyDeniedClaim() throws Exception {
        UUID claimId = seedClaim("DENIED");

        mockMvc.perform(put("/claims/{claimId}/deny", claimId)
                        .with(adminUser()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("CLAIM_NOT_PENDING"));
    }

    @Test
    void denyClaimShouldReturnUnprocessableEntityForApprovedClaim() throws Exception {
        UUID claimId = seedClaim("APPROVED");

        mockMvc.perform(put("/claims/{claimId}/deny", claimId)
                        .with(adminUser()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("CLAIM_NOT_PENDING"));
    }

    @Test
    void denyClaimShouldReturnNotFoundForNonExistentClaim() throws Exception {
        UUID nonExistentClaimId = UUID.randomUUID();

        mockMvc.perform(put("/claims/{claimId}/deny", nonExistentClaimId)
                        .with(adminUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void denyClaimShouldReturnUnauthorizedWithoutToken() throws Exception {
        UUID claimId = seedClaim("PENDING");

        mockMvc.perform(put("/claims/{claimId}/deny", claimId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void denyClaimShouldReturnForbiddenWithoutPermission() throws Exception {
        UUID claimId = seedClaim("PENDING");

        mockMvc.perform(put("/claims/{claimId}/deny", claimId)
                        .with(jwtWithPermissions()))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor adminUser() {
        return jwtWithPermissions("update:claims");
    }

    private RequestPostProcessor jwtWithPermissions(String... permissions) {
        return jwt()
                .jwt(jwt -> jwt
                        .subject("auth0|admin-user")
                        .claim("permissions", List.of(permissions)))
                .authorities(jwt -> Objects.requireNonNull(
                        jwtAuthenticationConverter.convert(jwt)).getAuthorities());
    }

    private UUID seedClaim(String status) {
        UUID consumerId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();

        String personalId = UUID.randomUUID().toString().substring(0, 11);
        String email = "claimtest-" + UUID.randomUUID() + "@test.com";

        jdbcTemplate.update(
                "INSERT INTO consumers (id, version, idp_id, email, first_name, last_name, " +
                        "personal_id, date_of_birth, address, city, created_at, created_by, deleted_at) " +
                        "VALUES (?, 0, ?, ?, 'Test', 'User', ?, '2000-01-01', '123 Test St', 'Testville', " +
                        "NOW(), 'test', NULL)",
                consumerId, "auth0|claim-consumer-" + consumerId, email, personalId);

        jdbcTemplate.update(
                "INSERT INTO packages (id, version, name, payroll, start_date, end_date, status, " +
                        "created_at, created_by, deleted_at) " +
                        "VALUES (?, 0, 'Test Package', 'MONTHLY', '2024-01-01', '2025-01-01', " +
                        "'INITIALIZED', NOW(), 'test', NULL)",
                packageId);

        jdbcTemplate.update(
                "INSERT INTO plans (id, version, package_id, name, type, contribution, election, " +
                        "created_at, created_by, deleted_at) " +
                        "VALUES (?, 0, ?, 'Test Plan', 'HEALTH_INSURANCE', 100.00, 50.00, " +
                        "NOW(), 'test', NULL)",
                planId, packageId);

        jdbcTemplate.update(
                "INSERT INTO enrollments (id, version, consumer_id, plan_id, status, " +
                        "created_at, created_by, deleted_at) " +
                        "VALUES (?, 0, ?, ?, 'ACTIVE', NOW(), 'test', NULL)",
                enrollmentId, consumerId, planId);

        jdbcTemplate.update(
                "INSERT INTO claims (id, version, enrollment_id, service_date, amount, status, " +
                        "created_at, created_by, deleted_at) " +
                        "VALUES (?, 0, ?, '2026-07-01', 100.00, ?, NOW(), 'test', NULL)",
                claimId, enrollmentId, status);

        return claimId;
    }
}
