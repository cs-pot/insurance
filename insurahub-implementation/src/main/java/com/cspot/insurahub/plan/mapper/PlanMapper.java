package com.cspot.insurahub.plan.mapper;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.enumeration.PlanType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PlanMapper {

    public InsurancePlan toEntity(
            InsurancePackage insurancePackage,
            PlanRequest request
    ) {
        return new InsurancePlan(
                insurancePackage,
                request.getName(),
                PlanType.valueOf(request.getType().name()),
                BigDecimal.valueOf(request.getContribution()),
                BigDecimal.valueOf(request.getElection())
        );
    }
}
