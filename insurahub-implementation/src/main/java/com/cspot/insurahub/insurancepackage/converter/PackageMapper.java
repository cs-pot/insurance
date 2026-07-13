package com.cspot.insurahub.insurancepackage.converter;

import com.cspot.insurahub.insurancepackage.InsurancePackage;
import com.cspot.insurahub.insurancepackage.dto.PackageCreateDto;
import com.cspot.insurahub.model.PackageCreateRequest;
import com.cspot.insurahub.payroll.Payroll;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PackageMapper {

    InsurancePackage initializeFromCreateRequest(PackageCreateRequest request);

    InsurancePackage initializeFromCreateDto(PackageCreateDto dto);

    Payroll map(PackageCreateRequest.PayrollEnum payroll);

    default Payroll map(String payroll) {
        return payroll == null
                ? null
                : Payroll.valueOf(payroll);
    }
}