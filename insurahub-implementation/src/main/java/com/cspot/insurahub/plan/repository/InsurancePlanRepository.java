package com.cspot.insurahub.plan.repository;

import com.cspot.insurahub.plan.entity.InsurancePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, UUID> {
}
