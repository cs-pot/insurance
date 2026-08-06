package com.cspot.insurahub.plan.service;

import com.cspot.insurahub.auth.service.AuthenticationMetadataQueryService;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.insurancepackage.validation.PackageValidator;
import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.model.PlanResponse;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.mapper.PlanMapper;
import com.cspot.insurahub.plan.repository.InsurancePlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final InsurancePackageRepository packageRepository;
    private final InsurancePlanRepository planRepository;
    private final PlanMapper planMapper;
    private final PackageValidator packageValidator;
    private final AuthenticationMetadataQueryService authenticationMetadataQueryService;

    @Transactional
    public PostResponse addPlan(UUID packageId, PlanRequest request) {
        InsurancePackage insurancePackage = packageRepository.findByIdOrThrow(packageId);
        packageValidator.validateReadyForUpdate(insurancePackage);
        InsurancePlan plan = planMapper.toEntity(insurancePackage, request);
        plan = planRepository.save(plan);
        log.info("Plan added to package: packageId={}, planId={}", packageId, plan.getId());
        return new PostResponse(plan.getId());
    }

    @Transactional(readOnly = true)
    public Page<PlanResponse> getPackagePlans(UUID packageId, Pageable pageable) {
        if (!packageRepository.existsById(packageId)) {
            throw new PackageNotFoundException(packageId);
        }
        Page<InsurancePlan> plansPage = planRepository.findByInsurancePackageId(packageId, pageable);
        log.info("Returning page of {} plans of package {}", plansPage.getSize(), packageId);
        return plansPage.map(planMapper::toPlanResponse);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getAvailablePlans() {
        return planRepository.findByInsurancePackageStatus(InsurancePackageStatus.INITIALIZED)
                .stream()
                .map(planMapper::toPlanResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlanById(UUID id) {
        InsurancePlan plan = planRepository.findByIdOrThrow(id);
        return planMapper.toPlanResponse(plan);
    }

    @Transactional
    public void updatePlan(UUID id, PlanRequest planRequest) {
        logPlanUpdate(id, planRequest);

        InsurancePlan insurancePlan = planRepository.findByIdOrThrow(id);

        packageValidator.validateReadyForUpdate(insurancePlan.getInsurancePackage());
        planMapper.updateFromUpdateRequest(insurancePlan, planRequest);
    }

    private void logPlanUpdate(UUID id, PlanRequest planRequest) {
        log.debug(
                "Updating plan: id={}, name={}, type={}, contribution={}, election={}",
                id,
                planRequest.getName(),
                planRequest.getType(),
                planRequest.getContribution(),
                planRequest.getElection()
        );
    }

    @Transactional
    public void deletePlan(UUID planId) {
        InsurancePlan plan = planRepository.findByIdOrThrow(planId);
        packageValidator.validateReadyForUpdate(plan.getInsurancePackage());
        String deletedBy = authenticationMetadataQueryService.getRequiredAuthenticatedPrincipalName();
        plan.markDeleted(deletedBy);
        log.info("Plan deleted: id={}, packageId={}", planId, plan.getInsurancePackage().getId());
    }
}
