package com.cspot.insurahub.insurancepackage;

import com.cspot.insurahub.insurancepackage.converter.PackageMapper;
import com.cspot.insurahub.model.PostPackageRequest;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageService {

    private final InsurancePackageRepository repository;
    private final PackageMapper mapper;
    private final Clock clock;

    @Transactional
    public PostResponse createPackage(PostPackageRequest request) {
        InsurancePackage insurancePackage =
                mapper.initializeFromCreateRequest(request);
        PackageValidator packageValidator = new PackageValidator(clock);
        packageValidator.validate(insurancePackage);

        log.debug(
                "Creating package: name={}, payroll={}, startDate={}, endDate={}",
                request.getName(),
                request.getPayroll(),
                request.getStartDate(),
                request.getEndDate()
        );

        InsurancePackage savedPackage = repository.save(insurancePackage);
        log.info("Package created: Id={}", savedPackage.getId());


        return new PostResponse(savedPackage.getId());
    }

}
