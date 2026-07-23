package com.cspot.insurahub.plan.service;

import com.cspot.insurahub.model.PlanResponse;
import com.cspot.insurahub.plan.entity.Plan;
import com.cspot.insurahub.plan.entity.PlanStatus;
import com.cspot.insurahub.plan.exception.PlanNotFoundException;
import com.cspot.insurahub.plan.mapper.PlanMapper;
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
    private final PlanMapper planMapper;

    @Transactional(readOnly = true)
    public List<PlanResponse> getAvailablePlans() {
        List<Plan> plans = planRepository.findAllByStatus(PlanStatus.ACTIVE);
        return planMapper.toResponseList(plans);
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlanById(UUID id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with id: " + id));
        return planMapper.toResponse(plan);
    }
}
