package com.cspot.insurahub;

import com.cspot.insurahub.insurancepackage.InsurancePackageRepository;
import org.junit.jupiter.api.Test;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
class PackageIntegrationTest extends BaseIntegrationTest {

    private static final String PACKAGES_ENDPOINT = "/packages";
    private static final String PACKAGE_NAME = "Premium Health Package";
    private static final String PERMISSIONS_CLAIM = "permissions";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InsurancePackageRepository repository;

    @Autowired
    private Clock clock;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void shouldCreatePackage() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        assertPackageCreated(
                PACKAGE_NAME,
                "MONTHLY",
                startDate,
                endDate
        );
    }

    @ParameterizedTest
    @MethodSource("exactPayrollPeriods")
    void shouldCreatePackageWithExactInclusivePayrollPeriod(
            String payroll,
            LocalDate startDate,
            LocalDate endDate
    ) throws Exception {
        assertPackageCreated(
                PACKAGE_NAME,
                payroll,
                startDate,
                endDate
        );
    }

    @Test
    void shouldRejectPackageCreationWithoutAuthentication() throws Exception {
        long packagesBeforeRequest = repository.count();
        LocalDate startDate = LocalDate.now(clock).plusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        mockMvc.perform(post(PACKAGES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(
                                PACKAGE_NAME,
                                "MONTHLY",
                                startDate,
                                endDate
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.status").value(401));

        assertEquals(packagesBeforeRequest, repository.count());
    }

    @Test
    void shouldRejectPackageCreationWithoutCreatePermission() throws Exception {
        long packagesBeforeRequest = repository.count();
        LocalDate startDate = LocalDate.now(clock).plusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        mockMvc.perform(post(PACKAGES_ENDPOINT)
                        .with(jwtWithoutPermissions())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(
                                PACKAGE_NAME,
                                "MONTHLY",
                                startDate,
                                endDate
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.status").value(403));

        assertEquals(packagesBeforeRequest, repository.count());
    }

    @Test
    void shouldRejectPackageWithStartDateBeforeToday() throws Exception {
        LocalDate startDate = LocalDate.now(clock).minusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        assertPackageRejected(
                createRequestBody(
                        PACKAGE_NAME,
                        "MONTHLY",
                        startDate,
                        endDate
                ),
                "PACKAGE_START_DATE_IN_PAST",
                422
        );
    }

    @Test
    void shouldRejectPackageWithEndDateBeforeStartDate() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(2);
        LocalDate endDate = startDate.minusDays(1);

        assertPackageRejected(
                createRequestBody(
                        PACKAGE_NAME,
                        "MONTHLY",
                        startDate,
                        endDate
                ),
                "PACKAGE_END_DATE_BEFORE_START_DATE",
                422
        );
    }

    @ParameterizedTest
    @MethodSource("periodsShorterThanPayrollCycle")
    void shouldRejectPackageWithPeriodShorterThanPayrollCycle(
            String payroll,
            LocalDate startDate,
            LocalDate endDate
    ) throws Exception {
        assertPackageRejected(
                createRequestBody(
                        PACKAGE_NAME,
                        payroll,
                        startDate,
                        endDate
                ),
                "PACKAGE_PERIOD_TOO_SHORT",
                422
        );
    }

    @Test
    void shouldRejectPackageWithoutName() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        assertPackageRejected("""
                {
                  "payroll": "MONTHLY",
                  "startDate": "%s",
                  "endDate": "%s"
                }
                """.formatted(startDate, endDate),
                "VALIDATION_FAILED",
                400
        );
    }

    @Test
    void shouldRejectPackageWithBlankName() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        assertPackageRejected(
                createRequestBody(
                        "   ",
                        "MONTHLY",
                        startDate,
                        endDate
                ),
                "VALIDATION_FAILED",
                400
        );
    }

    @Test
    void shouldRejectPackageWithoutPayroll() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        assertPackageRejected("""
                {
                  "name": "Premium Health Package",
                  "startDate": "%s",
                  "endDate": "%s"
                }
                """.formatted(startDate, endDate),
                "VALIDATION_FAILED",
                400
        );
    }

    @Test
    void shouldRejectPackageWithBlankPayroll() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        assertPackageRejected(
                createRequestBody(
                        PACKAGE_NAME,
                        "   ",
                        startDate,
                        endDate
                ),
                "MALFORMED_REQUEST_BODY",
                400
        );
    }

    @Test
    void shouldRejectPackageWithInvalidPayroll() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        assertPackageRejected(
                createRequestBody(
                        PACKAGE_NAME,
                        "DAILY",
                        startDate,
                        endDate
                ),
                "MALFORMED_REQUEST_BODY",
                400
        );
    }

    @Test
    void shouldRejectPackageWithNonStringPayroll() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);
        LocalDate endDate = startDate.plusMonths(1);

        assertPackageRejected("""
                {
                  "name": "Premium Health Package",
                  "payroll": 123,
                  "startDate": "%s",
                  "endDate": "%s"
                }
                """.formatted(startDate, endDate),
                "MALFORMED_REQUEST_BODY",
                400
        );
    }

    @Test
    void shouldRejectPackageWithoutStartDate() throws Exception {
        LocalDate endDate = LocalDate.now(clock).plusMonths(1);

        assertPackageRejected("""
                {
                  "name": "Premium Health Package",
                  "payroll": "MONTHLY",
                  "endDate": "%s"
                }
                """.formatted(endDate),
                "VALIDATION_FAILED",
                400
        );
    }

    @Test
    void shouldRejectPackageWithInvalidStartDateFormat() throws Exception {
        LocalDate endDate = LocalDate.now(clock).plusMonths(1);

        assertPackageRejected("""
                {
                  "name": "Premium Health Package",
                  "payroll": "MONTHLY",
                  "startDate": "tomorrow",
                  "endDate": "%s"
                }
                """.formatted(endDate),
                "MALFORMED_REQUEST_BODY",
                400
        );
    }

    @Test
    void shouldRejectPackageWithoutEndDate() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);

        assertPackageRejected("""
                {
                  "name": "Premium Health Package",
                  "payroll": "MONTHLY",
                  "startDate": "%s"
                }
                """.formatted(startDate),
                "VALIDATION_FAILED",
                400
        );
    }

    @Test
    void shouldRejectPackageWithInvalidEndDateFormat() throws Exception {
        LocalDate startDate = LocalDate.now(clock).plusDays(1);

        assertPackageRejected("""
                {
                  "name": "Premium Health Package",
                  "payroll": "MONTHLY",
                  "startDate": "%s",
                  "endDate": "next month"
                }
                """.formatted(startDate),
                "MALFORMED_REQUEST_BODY",
                400
        );
    }

    @Test
    void shouldRejectPackageWithMultipleInvalidBodyValues() throws Exception {
        LocalDate endDate = LocalDate.now(clock).plusMonths(1);

        assertPackageRejected("""
                {
                  "name": "   ",
                  "payroll": "DAILY",
                  "endDate": "%s"
                }
                """.formatted(endDate),
                "MALFORMED_REQUEST_BODY",
                400
        );
    }

    @Test
    void shouldRejectPackageWithUnsupportedContentType() throws Exception {
        long packagesBeforeRequest = repository.count();

        mockMvc.perform(post(PACKAGES_ENDPOINT)
                        .with(jwtWithPermissions("create:packages"))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=Premium Health Package"))
                .andExpect(status().isUnsupportedMediaType());

        assertEquals(packagesBeforeRequest, repository.count());
    }

    @Test
    void shouldRejectPackageWithoutRequestBody() throws Exception {
        assertPackageRejected(
                null,
                "MALFORMED_REQUEST_BODY",
                400
        );
    }

    private void assertPackageCreated(
            String name,
            String payroll,
            LocalDate startDate,
            LocalDate endDate
    ) throws Exception {
        long packagesBeforeRequest = repository.count();

        mockMvc.perform(post(PACKAGES_ENDPOINT)
                        .with(jwtWithPermissions("create:packages"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(
                                name,
                                payroll,
                                startDate,
                                endDate
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        assertEquals(packagesBeforeRequest + 1, repository.count());
    }

    private void assertPackageRejected(
            String requestBody,
            String expectedCode,
            int expectedStatus
    ) throws Exception {
        long packagesBeforeRequest = repository.count();

        var request = post(PACKAGES_ENDPOINT)
                .with(jwtWithPermissions("create:packages"))
                .contentType(MediaType.APPLICATION_JSON);

        if (requestBody != null) {
            request.content(requestBody);
        }

        mockMvc.perform(request)
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.error").value(expectedCode))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.message").exists());

        assertEquals(packagesBeforeRequest, repository.count());
    }

    private String createRequestBody(
            String name,
            String payroll,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return """
                {
                  "name": "%s",
                  "payroll": "%s",
                  "startDate": "%s",
                  "endDate": "%s"
                }
                """.formatted(name, payroll, startDate, endDate);
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

    private static Stream<Arguments> exactPayrollPeriods() {
        return Stream.of(
                Arguments.of(
                        "WEEKLY",
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 7, 16)
                ),
                Arguments.of(
                        "BI_WEEKLY",
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 7, 23)
                ),
                Arguments.of(
                        "MONTHLY",
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 8, 9)
                ),
                Arguments.of(
                        "MONTHLY",
                        LocalDate.of(2027, 1, 31),
                        LocalDate.of(2027, 2, 27)
                )
        );
    }

    private static Stream<Arguments> periodsShorterThanPayrollCycle() {
        return Stream.of(
                Arguments.of(
                        "WEEKLY",
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 7, 15)
                ),
                Arguments.of(
                        "BI_WEEKLY",
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 7, 22)
                ),
                Arguments.of(
                        "MONTHLY",
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 8, 8)
                )
        );
    }
}