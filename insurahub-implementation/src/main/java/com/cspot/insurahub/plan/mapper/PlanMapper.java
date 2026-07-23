package com.cspot.insurahub.plan.mapper;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.model.PlanResponse;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PlanMapper {

    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "contribution", source = "request.contribution")
    @Mapping(target = "election", source = "request.election")
    InsurancePlan toEntity(InsurancePackage insurancePackage, PlanRequest request);

    PlanResponse toPlanResponse(InsurancePlan plan);
}
