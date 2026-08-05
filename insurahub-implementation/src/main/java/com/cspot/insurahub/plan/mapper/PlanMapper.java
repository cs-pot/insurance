package com.cspot.insurahub.plan.mapper;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.model.PlanResponse;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PlanMapper {

    @Mapping(target = "name", source = "request.name")
    InsurancePlan toEntity(InsurancePackage insurancePackage, PlanRequest request);

    PlanResponse toPlanResponse(InsurancePlan plan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "insurancePackage", ignore = true)
    void updateFromUpdateRequest(@MappingTarget InsurancePlan insurancePlan, PlanRequest request);
}
