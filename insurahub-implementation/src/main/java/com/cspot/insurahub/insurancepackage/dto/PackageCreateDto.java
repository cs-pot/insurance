package com.cspot.insurahub.insurancepackage.dto;

import com.cspot.insurahub.payroll.Payroll;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PackageCreateDto {

    @NotBlank(message = "PACKAGE_NAME_REQUIRED")
    private String name;

    @NotBlank(message = "PACKAGE_PAYROLL_REQUIRED")
    @Pattern(regexp = Payroll.ALLOWED_VALUES_REGEX, message = "PACKAGE_PAYROLL_INVALID")
    private String payroll;

    @Setter
    @NotNull(message = "PACKAGE_START_DATE_REQUIRED")
    private LocalDate startDate;

    @Setter
    @NotNull(message = "PACKAGE_END_DATE_REQUIRED")
    private LocalDate endDate;

    public void setName(Object name) {
        this.name = toStringOrNull(name);
    }

    public void setPayroll(Object payroll) {
        this.payroll = toStringOrNull(payroll);
    }

    private String toStringOrNull(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String text) {
            return text;
        }

        throw new IllegalArgumentException("Value must be a string");
    }
}
