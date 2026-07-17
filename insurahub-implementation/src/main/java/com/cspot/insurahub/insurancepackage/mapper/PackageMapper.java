package com.cspot.insurahub.insurancepackage.mapper;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.model.PostPackageRequest;
import com.cspot.insurahub.payroll.Payroll;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PackageMapper {

    @Mapping(target = "status", ignore = true)
    InsurancePackage initializeFromCreateRequest(PostPackageRequest request);

    Payroll map(PostPackageRequest.PayrollEnum payroll);

    default Payroll map(String payroll) {
        return payroll == null
                ? null
                : Payroll.valueOf(payroll);
    }
}
