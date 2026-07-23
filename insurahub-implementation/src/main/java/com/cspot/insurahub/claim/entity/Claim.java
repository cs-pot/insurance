package com.cspot.insurahub.claim.entity;

import com.cspot.insurahub.claim.enumeration.ClaimStatus;
import com.cspot.insurahub.common.SoftDeletableAuditableEntity;
import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "claims")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Claim extends SoftDeletableAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, updatable = false)
    private Consumer employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false, updatable = false)
    private InsurancePlan plan;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ClaimStatus status;

    public Claim(
            Consumer employee,
            InsurancePlan plan,
            LocalDate serviceDate,
            BigDecimal amount
    ) {
        this.employee = employee;
        this.plan = plan;
        this.serviceDate = serviceDate;
        this.amount = amount;
        this.status = ClaimStatus.PENDING;
    }
}
