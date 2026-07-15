package com.cspot.insurahub.insurancepackage.converter;

import com.cspot.insurahub.insurancepackage.InsurancePackage;
import com.cspot.insurahub.model.PostPackageRequest;
import com.cspot.insurahub.payroll.Payroll;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PackageMapper {

    InsurancePackage initializeFromCreateRequest(PostPackageRequest request);

    Payroll map(PostPackageRequest.PayrollEnum payroll);

    default Payroll map(String payroll) {
        return payroll == null
                ? null
                : Payroll.valueOf(payroll);
    }
}