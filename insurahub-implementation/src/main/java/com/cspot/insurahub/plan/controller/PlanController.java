package com.cspot.insurahub.plan.controller;

import com.cspot.insurahub.api.PlansApi;
import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.model.PlanResponse;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlanController implements PlansApi {

    private final PlanService planService;

    @Override
    @PreAuthorize("hasAuthority('view:packages')")
    public Page<PlanResponse> getPackagePlans(UUID packageId, Pageable pageable) {
        return planService.getPackagePlans(packageId, pageable);
    }

    @Override
    @PreAuthorize("hasAuthority('update:packages')")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse addPackagePlan(UUID packageId, PlanRequest planRequest) {
        return planService.addPlan(packageId, planRequest);
    }
}