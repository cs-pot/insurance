package com.cspot.insurahub.plan.controller;

import com.cspot.insurahub.api.PlansApi;
import com.cspot.insurahub.model.PlanResponse;
import com.cspot.insurahub.plan.entity.Plan;
import com.cspot.insurahub.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlanController implements PlansApi {

    private final PlanService planService;

    @Override
    public List<PlanResponse> getPlans() {
        List<Plan> plans = planService.getAvailablePlans();
        
        return plans.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PlanResponse getPlanById(UUID id) {
        Plan plan = planService.getPlanById(id);
        return mapToResponse(plan);
    }

    private PlanResponse mapToResponse(Plan plan) {
        return new PlanResponse()
                .id(plan.getId())
                .name(plan.getName())
                .type(plan.getType())
                .electionAmount(plan.getElectionAmount().doubleValue())
                .contributionAmount(plan.getContributionAmount().doubleValue())
                .coverageDetails(plan.getCoverageDetails())
                .status(PlanResponse.StatusEnum.fromValue(plan.getStatus().name()));
    }
}
