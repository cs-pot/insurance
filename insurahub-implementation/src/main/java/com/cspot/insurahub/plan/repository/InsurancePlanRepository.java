package com.cspot.insurahub.plan.repository;

import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.exception.PlanNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, UUID> {

    Page<InsurancePlan> findByInsurancePackageId(UUID packageId, Pageable pageable);

    List<InsurancePlan> findByInsurancePackageStatus(InsurancePackageStatus status);

    default InsurancePlan findByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(PlanNotFoundException::new);
    }
}
