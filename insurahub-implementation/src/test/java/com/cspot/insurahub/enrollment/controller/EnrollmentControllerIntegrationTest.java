package com.cspot.insurahub.enrollment.controller;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
class EnrollmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void getEnrollmentsWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/enrollments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getEnrollmentsShouldReturnSeededDataForAuthenticatedConsumer() throws Exception {
        UUID consumerId = seedConsumer("auth0|consumer-A");
        UUID planId = seedPlan();
        seedEnrollment(consumerId, planId, "ACTIVE");

        mockMvc.perform(get("/enrollments")
                        .with(jwtWithPermission("view:own:enrollments", "auth0|consumer-A")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].planName").value("Test Plan"))
                .andExpect(jsonPath("$[0].planType").value("HEALTH_INSURANCE"))
                .andExpect(jsonPath("$[0].electionAmount").value(50.0))
                .andExpect(jsonPath("$[0].contributionAmount").value(100.0))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getEnrollmentsShouldFilterByStatus() throws Exception {
        UUID consumerId = seedConsumer("auth0|consumer-B");
        UUID planId = seedPlan();
        seedEnrollment(consumerId, planId, "ACTIVE");
        seedEnrollment(consumerId, planId, "CANCELLED");

        mockMvc.perform(get("/enrollments")
                        .with(jwtWithPermission("view:own:enrollments", "auth0|consumer-B")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        mockMvc.perform(get("/enrollments").param("status", "CANCELLED")
                        .with(jwtWithPermission("view:own:enrollments", "auth0|consumer-B")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    @Test
    void getEnrollmentsShouldReturnEmptyArrayForConsumerWithNoEnrollments() throws Exception {
        seedConsumer("auth0|consumer-C");

        mockMvc.perform(get("/enrollments")
                        .with(jwtWithPermission("view:own:enrollments", "auth0|consumer-C")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEnrollmentsShouldIsolateDataByConsumer() throws Exception {
        UUID consumerAId = seedConsumer("auth0|consumer-A");
        seedConsumer("auth0|consumer-B");
        UUID planId = seedPlan();
        seedEnrollment(consumerAId, planId, "ACTIVE");

        mockMvc.perform(get("/enrollments")
                        .with(jwtWithPermission("view:own:enrollments", "auth0|consumer-B")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private RequestPostProcessor jwtWithPermission(String permission, String subject) {
        return jwt()
                .jwt(jwt -> jwt
                        .subject(subject)
                        .claim("permissions", List.of(permission)))
                .authorities(jwt -> Objects.requireNonNull(
                        jwtAuthenticationConverter.convert(jwt)).getAuthorities());
    }

    private UUID seedConsumer(String idpId) {
        UUID consumerId = UUID.randomUUID();
        String personalId = UUID.randomUUID().toString().substring(0, 11);
        String email = idpId.replace("|", "-") + "_" + UUID.randomUUID() + "@test.com";

        String sql = "INSERT INTO consumers (id, version, idp_id, email, first_name, " +
                "last_name, personal_id, date_of_birth, address, city, " +
                "created_at, created_by, deleted_at) " +
                "VALUES (?, 0, ?, ?, 'Test', 'User', ?, '2000-01-01', '123 Test St', 'Testville', " +
                "NOW(), 'test', NULL)";
        
        jdbcTemplate.update(sql, consumerId, idpId, email, personalId);
        return consumerId;
    }

    private UUID seedPlan() {
        UUID packageId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        String packageSql = "INSERT INTO packages (id, version, name, payroll, start_date, " +
                "end_date, status, created_at, created_by, deleted_at) " +
                "VALUES (?, 0, 'Test Package', 'MONTHLY', '2024-01-01', '2025-01-01', " +
                "'INITIALIZED', NOW(), 'test', NULL)";
        jdbcTemplate.update(packageSql, packageId);

        String planSql = "INSERT INTO plans (id, version, package_id, name, type, " +
                "contribution, election, created_at, created_by, deleted_at) " +
                "VALUES (?, 0, ?, 'Test Plan', 'HEALTH_INSURANCE', 100.00, 50.00, " +
                "NOW(), 'test', NULL)";
        jdbcTemplate.update(planSql, planId, packageId);

        return planId;
    }

    private void seedEnrollment(UUID consumerId, UUID planId, String status) {
        String sql = "INSERT INTO enrollments (id, version, consumer_id, plan_id, status, " +
                "created_at, created_by, deleted_at) " +
                "VALUES (?, 0, ?, ?, ?, NOW(), 'test', NULL)";
        jdbcTemplate.update(sql, UUID.randomUUID(), consumerId, planId, status);
    }
}
