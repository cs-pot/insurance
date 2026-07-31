package com.cspot.insurahub.enrollment.mapper;

import com.cspot.insurahub.enrollment.entity.Enrollment;
import com.cspot.insurahub.model.EnrollmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EnrollmentMapper {

    @Mapping(target = "planName", source = "plan.name")
    @Mapping(target = "planType", source = "plan.type")
    @Mapping(target = "electionAmount", source = "plan.election")
    @Mapping(target = "contributionAmount", source = "plan.contribution")
    EnrollmentResponse toResponse(Enrollment enrollment);

    List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments);
}
