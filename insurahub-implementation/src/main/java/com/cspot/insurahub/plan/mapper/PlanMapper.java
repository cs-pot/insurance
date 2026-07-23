package com.cspot.insurahub.plan.mapper;

import com.cspot.insurahub.model.PlanResponse;
import com.cspot.insurahub.plan.entity.Plan;
import com.cspot.insurahub.plan.entity.PlanStatus;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlanMapper {

    PlanResponse toResponse(Plan plan);

    List<PlanResponse> toResponseList(List<Plan> plans);

    default PlanResponse.StatusEnum map(PlanStatus status) {
        if (status == null) {
            return null;
        }
        return PlanResponse.StatusEnum.fromValue(status.name());
    }
}
