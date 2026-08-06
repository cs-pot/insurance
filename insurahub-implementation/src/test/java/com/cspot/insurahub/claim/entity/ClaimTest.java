package com.cspot.insurahub.claim.entity;

import com.cspot.insurahub.claim.enumeration.ClaimStatus;
import com.cspot.insurahub.claim.exception.ClaimNotPendingException;
import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.enrollment.entity.Enrollment;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.model.PlanType;
import com.cspot.insurahub.payroll.Payroll;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClaimTest {

    @Test
    void shouldCreateClaimWithPendingStatus() {
        Enrollment enrollment = enrollment();

        LocalDate serviceDate = LocalDate.now();
        BigDecimal amount = BigDecimal.valueOf(999.99);

        Claim claim = new Claim(enrollment, serviceDate, amount);

        assertEquals(enrollment, claim.getEnrollment());
        assertEquals(serviceDate, claim.getServiceDate());
        assertEquals(amount, claim.getAmount());
        assertEquals(ClaimStatus.PENDING, claim.getStatus());
        assertEquals(claim, enrollment.getClaims().getFirst());
    }

    @Test
    void shouldDenyPendingClaim() {
        Claim claim = new Claim(enrollment(), LocalDate.now(), BigDecimal.valueOf(100));

        claim.deny();

        assertEquals(ClaimStatus.DENIED, claim.getStatus());
    }

    @Test
    void shouldThrowWhenDenyingAlreadyDeniedClaim() {
        Claim claim = new Claim(enrollment(), LocalDate.now(), BigDecimal.valueOf(100));
        claim.deny();

        assertThrows(ClaimNotPendingException.class, claim::deny);
    }

    private Enrollment enrollment() {
        return new Enrollment(consumer(), insurancePlan());
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
