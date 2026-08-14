package com.cspot.insurahub.denialreason.mapper;

import com.cspot.insurahub.denialreason.entity.DenialReason;
import com.cspot.insurahub.model.DenialReasonResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface DenialReasonMapper {

    DenialReasonResponse toResponse(DenialReason denialReason);
}
