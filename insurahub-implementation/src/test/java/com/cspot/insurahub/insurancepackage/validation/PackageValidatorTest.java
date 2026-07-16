package com.cspot.insurahub.insurancepackage.validation;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.exception.InvalidPackageException;
import com.cspot.insurahub.insurancepackage.exception.PackageAlreadyInitializedException;
import com.cspot.insurahub.model.PackageRequest;
import com.cspot.insurahub.payroll.Payroll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PackageValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 15);

    @Mock
    private Clock clock = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @InjectMocks
    private PackageValidator packageValidator;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                TODAY.atStartOfDay().toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );
        packageValidator = new PackageValidator(clock);
    }

    @Test
    void shouldValidateCreateRequestWhenAllFieldsAreValid() {
        PackageRequest request = new PackageRequest(
                "Premium Health Package",
                Payroll.MONTHLY,
                TODAY,
                TODAY.plusMonths(1).minusDays(1)
        );

        assertDoesNotThrow(() -> packageValidator.validate(request));
    }

    @Test
    void shouldValidateInsurancePackageWhenAllFieldsAreValid() {
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                TODAY,
                TODAY.plusMonths(1).minusDays(1)
        );

        assertDoesNotThrow(() -> packageValidator.validate(insurancePackage));
    }

    @Test
    void shouldRejectBlankNameForCreateRequest() {
        PackageRequest request = new PackageRequest(
                "   ",
                Payroll.MONTHLY,
                TODAY,
                TODAY.plusMonths(1).minusDays(1)
        );

        InvalidPackageException exception = assertThrows(
                InvalidPackageException.class,
                () -> packageValidator.validate(request)
        );

        assertThat(exception.getCode()).isEqualTo("PACKAGE_NAME_REQUIRED");
        assertThat(exception.getMessage()).isEqualTo("Name is required");
    }

    @Test
    void shouldRejectPastStartDateForCreateRequest() {
        PackageRequest request = new PackageRequest(
                "Premium Health Package",
                Payroll.MONTHLY,
                TODAY.minusDays(1),
                TODAY.plusMonths(1).minusDays(1)
        );

        InvalidPackageException exception = assertThrows(
                InvalidPackageException.class,
                () -> packageValidator.validate(request)
        );

        assertThat(exception.getCode()).isEqualTo("PACKAGE_START_DATE_IN_PAST");
        assertThat(exception.getMessage()).isEqualTo("Start date must not be before today");
    }

    @Test
    void shouldRejectEndDateBeforeStartDateForCreateRequest() {
        PackageRequest request = new PackageRequest(
                "Premium Health Package",
                Payroll.MONTHLY,
                TODAY,
                TODAY.minusDays(1)
        );

        InvalidPackageException exception = assertThrows(
                InvalidPackageException.class,
                () -> packageValidator.validate(request)
        );

        assertThat(exception.getCode()).isEqualTo("PACKAGE_END_DATE_BEFORE_START_DATE");
        assertThat(exception.getMessage()).isEqualTo("End date must be after or equal to start date");
    }

    @Test
    void shouldRejectTooShortPayrollPeriodForCreateRequest() {
        PackageRequest request = new PackageRequest(
                "Premium Health Package",
                Payroll.MONTHLY,
                TODAY,
                TODAY.plusDays(10)
        );

        InvalidPackageException exception = assertThrows(
                InvalidPackageException.class,
                () -> packageValidator.validate(request)
        );

        assertThat(exception.getCode()).isEqualTo("PACKAGE_PERIOD_TOO_SHORT");
        assertThat(exception.getMessage()).isEqualTo(
                "Package period must contain at least one full payroll cycle"
        );
    }

    @Test
    void shouldAllowMinimumMonthlyPayrollPeriodForCreateRequest() {
        PackageRequest request = new PackageRequest(
                "Premium Health Package",
                Payroll.MONTHLY,
                TODAY,
                TODAY.plusMonths(1).minusDays(1)
        );

        assertDoesNotThrow(() -> packageValidator.validate(request));
    }

    @Test
    void shouldAllowMonthlyPackageWithoutPayrollValidationOnEntityWhenPayrollIsNull() {
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                null,
                TODAY,
                TODAY.plusDays(1)
        );

        assertDoesNotThrow(() -> packageValidator.validate(insurancePackage));
    }

    @Test
    void shouldRejectAlreadyInitializedPackage() {
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );
        insurancePackage.setStatus(com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus.INITIALIZED);

        PackageAlreadyInitializedException exception = assertThrows(
                PackageAlreadyInitializedException.class,
                () -> packageValidator.validateReadyForInitialization(insurancePackage)
        );

        assertThat(exception.getCode())
                .isEqualTo("PACKAGE_ALREADY_INITIALIZED");
    }

    @Test
    void shouldRejectInitializePackageWhenStartDateIsInPast() {
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 6, 8),
                LocalDate.of(2026, 7, 8)
        );

        InvalidPackageException exception = assertThrows(
                InvalidPackageException.class,
                () -> packageValidator.validateReadyForInitialization(insurancePackage)
        );

        assertThat(exception.getCode())
                .isEqualTo("PACKAGE_END_DATE_IN_PAST");
    }
}
