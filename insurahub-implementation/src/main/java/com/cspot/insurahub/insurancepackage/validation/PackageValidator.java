package com.cspot.insurahub.insurancepackage.validation;

import com.cspot.insurahub.common.exception.DomainValidationException;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.insurancepackage.exception.PackageUpdateNotAllowedException;
import com.cspot.insurahub.model.PackageRequest;
import com.cspot.insurahub.payroll.Payroll;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PackageValidator {

    private final Clock clock;

    public void validate(PackageRequest request) {
        validate(
                request.getName(),
                request.getPayroll(),
                request.getStartDate(),
                request.getEndDate()
        );
    }

    public void validate(InsurancePackage insurancePackage) {
        validate(
                insurancePackage.getName(),
                insurancePackage.getPayroll(),
                insurancePackage.getStartDate(),
                insurancePackage.getEndDate()
        );
    }

    public void validateReadyForInitialization(InsurancePackage insurancePackage) {
        if (isInitialized(insurancePackage)) {
            throw new DomainValidationException(
                    "PACKAGE_ALREADY_INITIALIZED",
                    "Package is already initialized"
            );
        }

        validateEndDateNotInPast(insurancePackage.getEndDate());
    }

    public void validateReadyForUpdate(InsurancePackage insurancePackage) {
        if (insurancePackage.getStatus() != InsurancePackageStatus.NOT_STARTED) {
            throw new PackageUpdateNotAllowedException(
                    "Package updates are only allowed when the status is NOT_STARTED"
            );
        }
    }

    private boolean isInitialized(InsurancePackage insurancePackage) {
        return insurancePackage.getStatus() == InsurancePackageStatus.INITIALIZED;
    }

    private void validate(
            String name,
            Payroll payroll,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateName(name);
        validateStartDateNotInPast(startDate);
        validateEndDate(startDate, endDate);
        validatePayrollPeriod(payroll, startDate, endDate);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException(
                    "PACKAGE_NAME_REQUIRED",
                    "Name is required"
            );
        }
    }

    private void validateStartDateNotInPast(LocalDate startDate) {
        LocalDate today = LocalDate.now(clock);

        if (startDate.isBefore(today)) {
            throw new DomainValidationException(
                    "PACKAGE_START_DATE_IN_PAST",
                    "Start date must not be before today"
            );
        }
    }

    private void validateEndDate(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new DomainValidationException(
                    "PACKAGE_END_DATE_BEFORE_START_DATE",
                    "End date must be after or equal to start date"
            );
        }
    }

    private void validateEndDateNotInPast(LocalDate endDate) {
        LocalDate today = LocalDate.now(clock);

        if (endDate.isBefore(today)) {
            throw new DomainValidationException(
                    "PACKAGE_END_DATE_IN_PAST",
                    "End date must not be before today"
            );
        }
    }

    private void validatePayrollPeriod(
            Payroll payroll,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (payroll == null || startDate == null || endDate == null) {
            return;
        }

        LocalDate minimumEndDate =
                payroll.minimumInclusiveEndDate(startDate);

        if (endDate.isBefore(minimumEndDate)) {
            throw new DomainValidationException(
                    "PACKAGE_PERIOD_TOO_SHORT",
                    "Package period must contain at least one full payroll cycle"
            );
        }
    }
}
