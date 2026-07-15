package com.cspot.insurahub.insurancepackage;

import com.cspot.insurahub.insurancepackage.converter.PackageMapper;
import com.cspot.insurahub.model.PostPackageRequest;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageService {

    private final InsurancePackageRepository repository;
    private final PackageMapper mapper;
    private final PackageValidator packageValidator;

    @Transactional
    public PostResponse createPackage(PostPackageRequest request) {
        packageValidator.validate(request);

        InsurancePackage insurancePackage =
                mapper.initializeFromCreateRequest(request);

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
