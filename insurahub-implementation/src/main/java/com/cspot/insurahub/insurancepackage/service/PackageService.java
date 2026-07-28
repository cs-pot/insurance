package com.cspot.insurahub.insurancepackage.service;

import com.cspot.insurahub.auth.service.AuthenticationMetadataQueryService;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
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
import org.springframework.security.authentication.InsufficientAuthenticationException;
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
    private final AuthenticationMetadataQueryService authenticationMetadataQueryService;

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
        InsurancePackage insurancePackage =
                packageRepository.findByIdOrThrow(packageId);

        packageValidator.validateReadyForInitialization(insurancePackage);

        insurancePackage.setStatus(InsurancePackageStatus.INITIALIZED);
    }

    @Transactional
    public void updatePackage(UUID id, PackageRequest packageRequest) {
        logPackageUpdate(id, packageRequest);
        InsurancePackage insurancePackage = packageRepository.findByIdOrThrow(id);

        packageValidator.validateReadyForUpdate(insurancePackage);
        packageMapper.updateFromUpdateRequest(insurancePackage, packageRequest);
        packageValidator.validate(insurancePackage);
    }

    @Transactional
    public void deletePackage(UUID packageId) {
        InsurancePackage insurancePackage = getPackageReadyForRemoval(packageId);
        String deletedBy = getDeletedBy();

        markPackageAndConnectedPlansDeleted(insurancePackage, deletedBy);
        logPackageDeleted(packageId, insurancePackage);
    }

    private InsurancePackage getPackageReadyForRemoval(UUID packageId) {
        InsurancePackage insurancePackage =
                packageRepository.findByIdOrThrow(packageId);

        packageValidator.validateReadyForRemoval(insurancePackage);

        return insurancePackage;
    }

    private void markPackageAndConnectedPlansDeleted(
            InsurancePackage insurancePackage,
            String deletedBy
    ) {
        insurancePackage.getPlans()
                .forEach(plan -> plan.markDeleted(deletedBy));
        insurancePackage.markDeleted(deletedBy);
    }

    private void logPackageDeleted(
            UUID packageId,
            InsurancePackage insurancePackage
    ) {
        log.info(
                "Package deleted: id={}, connectedPlans={}",
                packageId,
                insurancePackage.getPlans().size()
        );
    }

    private String getDeletedBy() {
        return authenticationMetadataQueryService.getAuthenticatedPrincipalName()
                .orElseThrow(() -> new InsufficientAuthenticationException(
                        "Principal name is required to delete a package"
                ));
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
