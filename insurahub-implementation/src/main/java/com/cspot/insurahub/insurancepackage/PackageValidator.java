package com.cspot.insurahub.insurancepackage;

import com.cspot.insurahub.payroll.Payroll;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class PackageValidator {

    private final Clock clock;

    public PackageValidator(Clock clock) {
        this.clock = clock;
    }

    public void validate(InsurancePackage insurancePackage) {
        validateName(insurancePackage.getName());
        validateStartDateNotInPast(insurancePackage.getStartDate());
        validateEndDate(
                insurancePackage.getStartDate(),
                insurancePackage.getEndDate()
        );
        validatePayrollPeriod(
                insurancePackage.getPayroll(),
                insurancePackage.getStartDate(),
                insurancePackage.getEndDate()
        );
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidPackageException(
                    "PACKAGE_NAME_REQUIRED",
                    "Name is required"
            );
        }
    }

    private void validateStartDateNotInPast(LocalDate startDate) {
        LocalDate today = LocalDate.now(clock);

        if (startDate.isBefore(today)) {
            throw new InvalidPackageException(
                    "PACKAGE_START_DATE_IN_PAST",
                    "Start date must not be before today"
            );
        }
    }

    private void validateEndDate(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidPackageException(
                    "PACKAGE_END_DATE_BEFORE_START_DATE",
                    "End date must be after or equal to start date"
            );
        }
    }

    private void validatePayrollPeriod(
            Payroll payroll,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate minimumEndDate = payroll.minimumInclusiveEndDate(startDate);

        if (endDate.isBefore(minimumEndDate)) {
            throw new InvalidPackageException(
                    "PACKAGE_PERIOD_TOO_SHORT",
                    "Package period must contain at least one full payroll cycle"
            );
        }
    }
}