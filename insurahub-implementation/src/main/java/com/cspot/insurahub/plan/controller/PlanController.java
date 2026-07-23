package com.cspot.insurahub.plan.controller;

import com.cspot.insurahub.api.PlansApi;
import com.cspot.insurahub.model.PlanResponse;
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
        return planService.getAvailablePlans();
    }

    @Override
    public PlanResponse getPlanById(UUID id) {
        return planService.getPlanById(id);
    }
}
