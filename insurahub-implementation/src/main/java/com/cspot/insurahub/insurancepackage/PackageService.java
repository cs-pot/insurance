package com.cspot.insurahub.insurancepackage;

import com.cspot.insurahub.insurancepackage.converter.PackageMapper;
import com.cspot.insurahub.model.PostPackageRequest;
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
    public UUID createPackage(PostPackageRequest request) {
        validateStartDateNotInPast(request.getStartDate());

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
}
