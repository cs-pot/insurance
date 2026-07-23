package com.cspot.insurahub.claim.entity;

import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.model.PlanType;
import com.cspot.insurahub.payroll.Payroll;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptTest {

    @Test
    void shouldCreateReceipt() {
        Claim claim = claim();
        byte[] content = "receipt".getBytes();

        Receipt receipt = new Receipt(
                claim,
                "receipt.pdf",
                "application/pdf",
                (long) content.length,
                content
        );

        assertEquals(claim, receipt.getClaim());
        assertEquals("receipt.pdf", receipt.getOriginalFileName());
        assertEquals("application/pdf", receipt.getContentType());
        assertEquals(content.length, receipt.getSizeBytes());
        assertArrayEquals(content, receipt.getContent());
    }

    private Claim claim() {
        return new Claim(
                consumer(),
                insurancePlan(),
                LocalDate.now(),
                BigDecimal.valueOf(100)
        );
    }

    private Consumer consumer() {
        Consumer consumer = new Consumer();
        consumer.setIdpId("idp-id");
        return consumer;
    }

    private InsurancePlan insurancePlan() {
        return new InsurancePlan(
                insurancePackage(),
                "Plan",
                PlanType.HEALTH_INSURANCE,
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(500)
        );
    }

    private InsurancePackage insurancePackage() {
        return new InsurancePackage(
                "Package",
                Payroll.MONTHLY,
                LocalDate.now(),
                LocalDate.now().plusMonths(1)
        );
    }
}