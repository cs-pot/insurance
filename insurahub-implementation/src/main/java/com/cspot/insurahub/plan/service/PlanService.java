package com.cspot.insurahub.plan.service;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.mapper.PlanMapper;
import com.cspot.insurahub.plan.repository.InsurancePlanRepository;
import com.cspot.insurahub.plan.validation.PlanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public PostResponse addPlan(UUID packageId, PlanRequest request) {
        InsurancePackage insurancePackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new PackageNotFoundException(packageId));

        planValidator.validate(request);

        InsurancePlan plan = planMapper.toEntity(insurancePackage, request);
        InsurancePlan savedPlan = planRepository.save(plan);

        log.info("Plan added to package: packageId={}, planId={}", packageId, savedPlan.getId());

        return new PostResponse(savedPlan.getId());
    }

}
