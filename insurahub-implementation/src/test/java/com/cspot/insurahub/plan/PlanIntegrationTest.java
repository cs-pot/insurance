package com.cspot.insurahub.plan;

import com.cspot.insurahub.BaseIntegrationTest;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.payroll.Payroll;
import com.cspot.insurahub.plan.repository.InsurancePlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class PlanIntegrationTest extends BaseIntegrationTest {

    private static final String PACKAGES_ENDPOINT = "/packages";
    private static final String PACKAGE_NAME = "Premium Health Package";
    private static final String PERMISSIONS_CLAIM = "permissions";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InsurancePackageRepository packageRepository;

    @Autowired
    private InsurancePlanRepository planRepository;

    @Autowired
    private Clock clock;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void shouldAddPlanToPackage() throws Exception {
        InsurancePackage insurancePackage = savePackage();
        long plansBeforeRequest = planRepository.count();

        mockMvc.perform(post(PACKAGES_ENDPOINT + "/" + insurancePackage.getId() + "/plans")
                        .with(jwtWithPermissions("update:packages"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanRequestBody(
                                "Standard Health",
                                "HEALTH_INSURANCE",
                                250,
                                500
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        assertEquals(plansBeforeRequest + 1, planRepository.count());
    }

    @Test
    void shouldRejectAddPlanToInitializedPackage() throws Exception {
        InsurancePackage insurancePackage = savePackage();
        insurancePackage.setStatus(InsurancePackageStatus.INITIALIZED);
        packageRepository.save(insurancePackage);
        long plansBeforeRequest = planRepository.count();

        mockMvc.perform(post(PACKAGES_ENDPOINT + "/" + insurancePackage.getId() + "/plans")
                        .with(jwtWithPermissions("update:packages"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanRequestBody(
                                "Vision Plus",
                                "VISION_INSURANCE",
                                120,
                                300
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PACKAGE_UPDATE_NOT_ALLOWED"))
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(plansBeforeRequest, planRepository.count());
    }

    @Test
    void shouldRejectAddPlanWithoutAuthentication() throws Exception {
        InsurancePackage insurancePackage = savePackage();
        long plansBeforeRequest = planRepository.count();

        mockMvc.perform(post(PACKAGES_ENDPOINT + "/" + insurancePackage.getId() + "/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanRequestBody(
                                "Standard Health",
                                "HEALTH_INSURANCE",
                                250,
                                500
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.status").value(401));

        assertEquals(plansBeforeRequest, planRepository.count());
    }

    @Test
    void shouldRejectAddPlanWithoutUpdatePermission() throws Exception {
        InsurancePackage insurancePackage = savePackage();
        long plansBeforeRequest = planRepository.count();

        mockMvc.perform(post(PACKAGES_ENDPOINT + "/" + insurancePackage.getId() + "/plans")
                        .with(jwtWithoutPermissions())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanRequestBody(
                                "Standard Health",
                                "HEALTH_INSURANCE",
                                250,
                                500
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.status").value(403));

        assertEquals(plansBeforeRequest, planRepository.count());
    }

    @Test
    void shouldRejectAddPlanWhenPackageDoesNotExist() throws Exception {
        UUID packageId = UUID.randomUUID();

        mockMvc.perform(post(PACKAGES_ENDPOINT + "/" + packageId + "/plans")
                        .with(jwtWithPermissions("update:packages"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanRequestBody(
                                "Standard Health",
                                "HEALTH_INSURANCE",
                                250,
                                500
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PACKAGE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldRejectAddPlanWithoutRequiredFields() throws Exception {
        InsurancePackage insurancePackage = savePackage();
        long plansBeforeRequest = planRepository.count();

        mockMvc.perform(post(PACKAGES_ENDPOINT + "/" + insurancePackage.getId() + "/plans")
                        .with(jwtWithPermissions("update:packages"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").exists());

        assertEquals(plansBeforeRequest, planRepository.count());
    }

    @ParameterizedTest
    @MethodSource("invalidPlanAmounts")
    void shouldRejectPlanAmountsOutsideAllowedRange(
            int contribution,
            int election
    ) throws Exception {
        InsurancePackage insurancePackage = savePackage();
        long plansBeforeRequest = planRepository.count();

        mockMvc.perform(post(PACKAGES_ENDPOINT + "/" + insurancePackage.getId() + "/plans")
                        .with(jwtWithPermissions("update:packages"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanRequestBody(
                                "Standard Health",
                                "HEALTH_INSURANCE",
                                contribution,
                                election
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        assertEquals(plansBeforeRequest, planRepository.count());
    }

    @Test
    void shouldRejectPlanNameWithInvalidCharacters() throws Exception {
        InsurancePackage insurancePackage = savePackage();
        long plansBeforeRequest = planRepository.count();

        mockMvc.perform(post(PACKAGES_ENDPOINT + "/" + insurancePackage.getId() + "/plans")
                        .with(jwtWithPermissions("update:packages"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlanRequestBody(
                                "Plan!",
                                "HEALTH_INSURANCE",
                                250,
                                500
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        assertEquals(plansBeforeRequest, planRepository.count());
    }

    @Test
    void shouldGetPlansOfPackage() throws Exception {
        InsurancePackage insurancePackage = savePackage();
        planRepository.saveAll(List.of(
                new com.cspot.insurahub.plan.entity.InsurancePlan(
                        insurancePackage,
                        "Standard Health",
                        com.cspot.insurahub.plan.enumeration.PlanType.HEALTH_INSURANCE,
                        java.math.BigDecimal.valueOf(250),
                        java.math.BigDecimal.valueOf(500)
                ),
                new com.cspot.insurahub.plan.entity.InsurancePlan(
                        insurancePackage,
                        "Dental Basic",
                        com.cspot.insurahub.plan.enumeration.PlanType.DENTAL_INSURANCE,
                        java.math.BigDecimal.valueOf(100),
                        java.math.BigDecimal.valueOf(300)
                )
        ));

        mockMvc.perform(get(PACKAGES_ENDPOINT + "/" + insurancePackage.getId() + "/plans")
                        .with(jwtWithPermissions("view:packages")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name").value(hasItems("Standard Health", "Dental Basic")))
                .andExpect(jsonPath("$[*].type").value(hasItems("HEALTH_INSURANCE", "DENTAL_INSURANCE")))
                .andExpect(jsonPath("$[*].contribution").value(hasItems(250, 100)))
                .andExpect(jsonPath("$[*].election").value(hasItems(500, 300)));
    }

    private String createPlanRequestBody(
            String name,
            String type,
            int contribution,
            int election
    ) {
        return """
                {
                  "name": "%s",
                  "type": "%s",
                  "contribution": %d,
                  "election": %d
                }
                """.formatted(name, type, contribution, election);
    }

    private InsurancePackage savePackage() {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);

        return packageRepository.save(new InsurancePackage(
                PACKAGE_NAME,
                Payroll.MONTHLY,
                startDate,
                startDate.plusMonths(1)
        ));
    }

    private RequestPostProcessor jwtWithPermissions(String... permissions) {
        return jwt()
                .jwt(jwt -> jwt.claim(
                        PERMISSIONS_CLAIM,
                        List.of(permissions)
                ))
                .authorities(this::convertAuthorities);
    }

    private RequestPostProcessor jwtWithoutPermissions() {
        return jwt()
                .jwt(jwt -> jwt.claim(
                        PERMISSIONS_CLAIM,
                        List.of()
                ))
                .authorities(this::convertAuthorities);
    }

    private Collection<GrantedAuthority> convertAuthorities(Jwt jwt) {
        return Objects.requireNonNull(
                jwtAuthenticationConverter.convert(jwt),
                "JWT authentication conversion must not return null"
        ).getAuthorities();
    }

    private Stream<Arguments> invalidPlanAmounts() {
        return Stream.of(
                Arguments.of(9, 500),
                Arguments.of(1001, 500),
                Arguments.of(250, 9),
                Arguments.of(250, 1001)
        );
    }
}
