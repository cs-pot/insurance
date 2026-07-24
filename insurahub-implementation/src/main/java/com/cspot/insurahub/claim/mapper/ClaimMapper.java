package com.cspot.insurahub.claim.mapper;

import com.cspot.insurahub.claim.entity.Claim;
import com.cspot.insurahub.model.ClaimResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public abstract class ClaimMapper {

    @Mapping(target = "consumerFullName",
            expression = "java(claim.getEmployee().getFirstName() + \" \" + claim.getEmployee().getLastName())")
    @Mapping(target = "planName", expression = "java(claim.getPlan().getName())")
    public abstract ClaimResponse toListItemResponse(Claim claim);
}