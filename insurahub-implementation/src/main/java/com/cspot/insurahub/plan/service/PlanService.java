package com.cspot.insurahub.plan.service;

import com.cspot.insurahub.plan.entity.Plan;
import com.cspot.insurahub.plan.entity.PlanStatus;
import com.cspot.insurahub.plan.exception.PlanNotFoundException;
import com.cspot.insurahub.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<Plan> getAvailablePlans() {
        return planRepository.findAllByStatus(PlanStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Plan getPlanById(UUID id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with id: " + id));
    }
}
