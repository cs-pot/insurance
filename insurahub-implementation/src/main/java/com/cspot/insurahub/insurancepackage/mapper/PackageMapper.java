package com.cspot.insurahub.insurancepackage.mapper;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.model.PackageRequest;
import com.cspot.insurahub.model.PackageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PackageMapper {

    PackageResponse toListItemResponse(InsurancePackage insurancePackage);

    @Mapping(target = "status", ignore = true)
    InsurancePackage initializeFromCreateRequest(PackageRequest request);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateFromUpdateRequest(@MappingTarget InsurancePackage insurancePackage, PackageRequest request);
}
