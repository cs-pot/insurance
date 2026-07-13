package com.cspot.insurahub.insurancepackage;

import com.cspot.insurahub.insurancepackage.converter.PackageMapper;
import com.cspot.insurahub.model.PackageCreateRequest;
import com.cspot.insurahub.payroll.Payroll;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PackageService {

    private final InsurancePackageRepository repository;
    private final PackageMapper mapper;
    private final Clock clock;

    @Transactional
    public UUID createPackage(PackageCreateRequest request) {
        Payroll payroll = Payroll.valueOf(request.getPayroll().getValue());

        validateStartDateNotInPast(request.getStartDate());
        validateName(request.getName());
        validateEndDate(request.getStartDate(), request.getEndDate());
        validatePayrollPeriod(
                payroll,
                request.getStartDate(),
                request.getEndDate()
        );

        InsurancePackage insurancePackage = mapper.initializeFromCreateRequest(request);

        return repository.save(insurancePackage).getId();
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

    private static void validateName(String name) {
        if (name.isBlank()) {
            throw new InvalidPackageException(
                    "PACKAGE_NAME_REQUIRED",
                    "Name is required"
            );
        }
    }

    private static void validateEndDate(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidPackageException(
                    "PACKAGE_END_DATE_BEFORE_START_DATE",
                    "End date must be after or equal to start date"
            );
        }
    }

    private static void validatePayrollPeriod(
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
