package com.cspot.insurahub.plan.service;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.insurancepackage.validation.PackageValidator;
import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.model.PlanResponse;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.mapper.PlanMapper;
import com.cspot.insurahub.plan.repository.InsurancePlanRepository;
import com.cspot.insurahub.plan.validation.PlanValidator;
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
public class PlanService {

    private final InsurancePackageRepository packageRepository;
    private final InsurancePlanRepository planRepository;
    private final PlanMapper planMapper;
    private final PlanValidator planValidator;
    private final PackageValidator packageValidator;

    @Transactional
    public PostResponse addPlan(UUID packageId, PlanRequest request) {
        InsurancePackage insurancePackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new PackageNotFoundException(packageId));

        packageValidator.validateReadyForUpdate(insurancePackage);
        planValidator.validate(request);

        InsurancePlan plan = planMapper.toEntity(insurancePackage, request);
        plan = planRepository.save(plan);

        log.info("Plan added to package: packageId={}, planId={}", packageId, plan.getId());

        return new PostResponse(plan.getId());
    }

    public Page<PlanResponse> getPackagePlans(UUID packageId, Pageable pageable) {
        InsurancePackage insurancePackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new PackageNotFoundException(packageId));
        Page<InsurancePlan> plansPage = planRepository.findByInsurancePackageId(packageId, pageable);
        log.info("Returning page of {} plans of package {}", plansPage.getSize(), packageId);
        return plansPage.map(planMapper::toPlanResponse);
    }
}
