package com.cspot.insurahub.claim.controller;

import com.cspot.insurahub.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
class ClaimControllerIntegrationTest extends BaseIntegrationTest {

    private static final String CONSUMERS_SEED = "/consumer/seed-consumers.sql";
    private static final String PACKAGES_SEED = "/package/seed-packages.sql";
    private static final String PLANS_SEED = "/plan/seed-plans.sql";
    private static final String ENROLLMENTS_SEED = "/enrollment/seed-enrollments.sql";
    private static final String CLAIMS_SEED = "/claim/seed-claims.sql";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    @Sql({CONSUMERS_SEED, PACKAGES_SEED, PLANS_SEED, ENROLLMENTS_SEED, CLAIMS_SEED})
    void shouldReturnClaims() throws Exception {
        mockMvc.perform(get("/claims")
                        .param("page", "0")
                        .param("size", "2")
                        .with(jwtWithPermission("view:claims")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("cccccccc-0001-0001-0001-000000000001"))
                .andExpect(jsonPath("$.content[0].consumerFullName").value("First Consumer"))
                .andExpect(jsonPath("$.content[0].serviceDate").value("2026-07-15"))
                .andExpect(jsonPath("$.content[0].planName").value("Standard Health"))
                .andExpect(jsonPath("$.content[0].amount").value(285.5))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[1].consumerFullName").value("Second Consumer"))
                .andExpect(jsonPath("$.content[1].planName").value("Dental Care"))
                .andExpect(jsonPath("$.content[1].status").value("APPROVED"))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(3));
    }

    @Test
    @Sql({CONSUMERS_SEED, PACKAGES_SEED, PLANS_SEED, ENROLLMENTS_SEED, CLAIMS_SEED})
    void shouldReturnRequestedClaimPage() throws Exception {
        mockMvc.perform(get("/claims")
                        .param("page", "1")
                        .param("size", "2")
                        .with(jwtWithPermission("view:claims")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("cccccccc-0003-0003-0003-000000000003"))
                .andExpect(jsonPath("$.content[0].status").value("REJECTED"))
                .andExpect(jsonPath("$.content[1].id").value("cccccccc-0004-0004-0004-000000000004"))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(3));
    }

    @Test
    @Sql({CONSUMERS_SEED, PACKAGES_SEED, PLANS_SEED, ENROLLMENTS_SEED, CLAIMS_SEED})
    void shouldReturnClaimsSortedByCreatedAtDescByDefault() throws Exception {
        mockMvc.perform(get("/claims")
                        .with(jwtWithPermission("view:claims")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("cccccccc-0001-0001-0001-000000000001"))
                .andExpect(jsonPath("$.content[1].id").value("cccccccc-0002-0002-0002-000000000002"))
                .andExpect(jsonPath("$.content[2].id").value("cccccccc-0003-0003-0003-000000000003"))
                .andExpect(jsonPath("$.content[3].id").value("cccccccc-0004-0004-0004-000000000004"))
                .andExpect(jsonPath("$.content[4].id").value("cccccccc-0005-0005-0005-000000000005"));
    }

    @Test
    void shouldReturnEmptyClaimList() throws Exception {
        mockMvc.perform(get("/claims")
                        .with(jwtWithPermission("view:claims")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    @Sql({CONSUMERS_SEED, PACKAGES_SEED, PLANS_SEED, ENROLLMENTS_SEED, CLAIMS_SEED})
    void shouldRejectClaimListWithoutAuthority() throws Exception {
        mockMvc.perform(get("/claims")
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor jwtWithPermission(String permission) {
        return jwt()
                .jwt(jwt -> jwt.claim("permissions", List.of(permission)))
                .authorities(jwt -> Objects.requireNonNull(jwtAuthenticationConverter.convert(jwt)).getAuthorities());
    }
}
