package com.cspot.insurahub.insurancepackage.entity;

import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.payroll.Payroll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InsurancePackageTest {

    @Test
    void shouldCreatePackageWithNotStartedStatus() {
        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 9);

        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                startDate,
                endDate
        );

        assertEquals("Premium Health Package", insurancePackage.getName());
        assertEquals(Payroll.MONTHLY, insurancePackage.getPayroll());
        assertEquals(startDate, insurancePackage.getStartDate());
        assertEquals(endDate, insurancePackage.getEndDate());
        assertEquals(InsurancePackageStatus.NOT_STARTED, insurancePackage.getStatus());
    }
}
