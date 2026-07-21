package com.cspot.insurahub.insurancepackage.service;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.insurancepackage.exception.PackageUpdateNotAllowedException;
import com.cspot.insurahub.insurancepackage.mapper.PackageMapper;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.insurancepackage.validation.PackageValidator;
import com.cspot.insurahub.model.PackageRequest;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageService {

    private final InsurancePackageRepository repository;
    private final PackageMapper mapper;
    private final PackageValidator packageValidator;

    @Transactional
    public PostResponse createPackage(PackageRequest request) {
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

    @Transactional
    public void initializePackage(UUID packageId) {
        InsurancePackage insurancePackage = repository.findById(packageId)
                .orElseThrow(() -> new PackageNotFoundException(packageId));

        packageValidator.validateReadyForInitialization(insurancePackage);

        insurancePackage.setStatus(InsurancePackageStatus.INITIALIZED);
    }

    @Transactional
    public void updatePackage(UUID id, PackageRequest packageRequest) {
        logPackageUpdate(id, packageRequest);
        InsurancePackage insurancePackage = repository.findById(id).orElseThrow(() -> new PackageNotFoundException(id));
        checkPackageStatusBeforeUpdate(insurancePackage);
        mapper.updateFromUpdateRequest(insurancePackage, packageRequest);
        packageValidator.validate(insurancePackage);
    }

    private void checkPackageStatusBeforeUpdate(InsurancePackage insurancePackage) {
        if (insurancePackage.getStatus() != InsurancePackageStatus.NOT_STARTED) {
            throw new PackageUpdateNotAllowedException("Package updates are only allowed when the status is " +
                    "NOT_STARTED");
        }
    }

    private void logPackageUpdate(UUID id, PackageRequest packageRequest) {
        log.debug("Updating package: id={}, name={}, payroll={}, startDate={}, endDate={}", id,
                packageRequest.getName(),
                packageRequest.getPayroll(),
                packageRequest.getStartDate(),
                packageRequest.getEndDate());
    }
}
