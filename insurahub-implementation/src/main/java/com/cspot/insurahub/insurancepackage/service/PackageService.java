package com.cspot.insurahub.insurancepackage.service;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.insurancepackage.exception.PackageUpdateNotAllowedException;
import com.cspot.insurahub.insurancepackage.mapper.PackageMapper;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.insurancepackage.validation.PackageValidator;
import com.cspot.insurahub.model.PackageRequest;
import com.cspot.insurahub.model.PackageResponse;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageService {

    private final InsurancePackageRepository packageRepository;
    private final PackageMapper packageMapper;
    private final PackageValidator packageValidator;

    @Transactional(readOnly = true)
    public Page<PackageResponse> getPackages(Pageable pageable) {
        return packageRepository.findAll(pageable)
                .map(packageMapper::toListItemResponse);
    }

    @Transactional
    public PostResponse createPackage(PackageRequest request) {
        packageValidator.validate(request);

        InsurancePackage insurancePackage =
                packageMapper.initializeFromCreateRequest(request);

        log.debug(
                "Creating package: name={}, payroll={}, startDate={}, endDate={}",
                request.getName(),
                request.getPayroll(),
                request.getStartDate(),
                request.getEndDate()
        );

        InsurancePackage savedPackage = packageRepository.save(insurancePackage);
        log.info("Package created: id={}", savedPackage.getId());

        return new PostResponse(savedPackage.getId());
    }

    @Transactional
    public void initializePackage(UUID packageId) {
        InsurancePackage insurancePackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new PackageNotFoundException(packageId));

        packageValidator.validateReadyForInitialization(insurancePackage);

        insurancePackage.setStatus(InsurancePackageStatus.INITIALIZED);
    }

    @Transactional
    public void updatePackage(UUID id, PackageRequest packageRequest) {
        logPackageUpdate(id, packageRequest);
        InsurancePackage insurancePackage = packageRepository.findById(id)
                .orElseThrow(() -> new PackageNotFoundException(id));

        checkPackageStatusBeforeUpdate(insurancePackage);
        packageMapper.updateFromUpdateRequest(insurancePackage, packageRequest);
        packageValidator.validate(insurancePackage);
    }

    private void checkPackageStatusBeforeUpdate(InsurancePackage insurancePackage) {
        if (insurancePackage.getStatus() != InsurancePackageStatus.NOT_STARTED) {
            throw new PackageUpdateNotAllowedException(
                    "Package updates are only allowed when the status is NOT_STARTED"
            );
        }
    }

    private void logPackageUpdate(UUID id, PackageRequest packageRequest) {
        log.debug(
                "Updating package: id={}, name={}, payroll={}, startDate={}, endDate={}",
                id,
                packageRequest.getName(),
                packageRequest.getPayroll(),
                packageRequest.getStartDate(),
                packageRequest.getEndDate()
        );
    }
}
