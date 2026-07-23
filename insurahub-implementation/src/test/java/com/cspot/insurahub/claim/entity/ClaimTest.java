package com.cspot.insurahub.claim.entity;

import com.cspot.insurahub.claim.enumeration.ClaimStatus;
import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.model.PlanType;
import com.cspot.insurahub.payroll.Payroll;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClaimTest {

    @Test
    void shouldCreateClaimWithPendingStatus() {
        Consumer employee = consumer();
        InsurancePlan plan = insurancePlan();

        LocalDate serviceDate = LocalDate.now();
        BigDecimal amount = BigDecimal.valueOf(999.99);

        Claim claim = new Claim(employee, plan, serviceDate, amount);

        assertEquals(employee, claim.getEmployee());
        assertEquals(plan, claim.getPlan());
        assertEquals(serviceDate, claim.getServiceDate());
        assertEquals(amount, claim.getAmount());
        assertEquals(ClaimStatus.PENDING, claim.getStatus());
    }

    private Consumer consumer() {
        Consumer consumer = new Consumer();
        consumer.setIdpId("idpId");
        return consumer;
    }

    private InsurancePlan insurancePlan() {
        return new InsurancePlan(
                insurancePackage(),
                "Plan Name",
                PlanType.HEALTH_INSURANCE,
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(500)
        );
    }

    private InsurancePackage insurancePackage() {
        return new InsurancePackage(
                "Package Name",
                Payroll.MONTHLY,
                LocalDate.now(),
                LocalDate.now().plusMonths(1)
        );
    }
}