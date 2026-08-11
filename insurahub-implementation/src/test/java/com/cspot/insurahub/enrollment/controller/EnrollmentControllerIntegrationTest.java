package com.cspot.insurahub.enrollment.controller;

import com.cspot.insurahub.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
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
@Sql(scripts = "classpath:sql/enrollment-test-data.sql")
class EnrollmentControllerIntegrationTest extends BaseIntegrationTest {

    private static final UUID CONSUMER_A_ID = UUID.fromString("a1111111-1111-1111-1111-111111111111");
    private static final UUID CONSUMER_B_ID = UUID.fromString("b2222222-2222-2222-2222-222222222222");
    private static final UUID PLAN_ID = UUID.fromString("e5555555-5555-5555-5555-555555555555");

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
        seedEnrollment(CONSUMER_A_ID, PLAN_ID, "ACTIVE");

        mockMvc.perform(get("/enrollments")
                        .with(jwtWithPermission("view:own:enrollments", "auth0|consumer-A")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].planName").value("Test Plan"))
                .andExpect(jsonPath("$.content[0].planType").value("HEALTH_INSURANCE"))
                .andExpect(jsonPath("$.content[0].electionAmount").value(50.0))
                .andExpect(jsonPath("$.content[0].contributionAmount").value(100.0))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    void getEnrollmentsShouldFilterByStatus() throws Exception {
        seedEnrollment(CONSUMER_B_ID, PLAN_ID, "ACTIVE");
        seedEnrollment(CONSUMER_B_ID, PLAN_ID, "CANCELLED");

        mockMvc.perform(get("/enrollments")
                        .with(jwtWithPermission("view:own:enrollments", "auth0|consumer-B")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

        mockMvc.perform(get("/enrollments").param("status", "CANCELLED")
                        .with(jwtWithPermission("view:own:enrollments", "auth0|consumer-B")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("CANCELLED"));
    }

    @Test
    void getEnrollmentsShouldIsolateDataByConsumer() throws Exception {
        seedEnrollment(CONSUMER_A_ID, PLAN_ID, "ACTIVE");

        mockMvc.perform(get("/enrollments")
                        .with(jwtWithPermission("view:own:enrollments", "auth0|consumer-B")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    private RequestPostProcessor jwtWithPermission(String permission, String subject) {
        return jwt()
                .jwt(jwt -> jwt
                        .subject(subject)
                        .claim("permissions", List.of(permission)))
                .authorities(jwt -> Objects.requireNonNull(
                        jwtAuthenticationConverter.convert(jwt)).getAuthorities());
    }

    private void seedEnrollment(UUID consumerId, UUID planId, String status) {
        String sql = "INSERT INTO enrollments (id, version, consumer_id, plan_id, status, " +
                "created_at, created_by, deleted_at) " +
                "VALUES (?, 0, ?, ?, ?, NOW(), 'test', NULL)";
        jdbcTemplate.update(sql, UUID.randomUUID(), consumerId, planId, status);
    }
}
