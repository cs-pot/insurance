package com.cspot.insurahub.plan.repository;

import com.cspot.insurahub.plan.entity.Plan;
import com.cspot.insurahub.plan.entity.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    List<Plan> findAllByStatus(PlanStatus status);
}
