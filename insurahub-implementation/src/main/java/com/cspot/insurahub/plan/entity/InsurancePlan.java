package com.cspot.insurahub.plan.entity;

import com.cspot.insurahub.common.SoftDeletableAuditableEntity;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.plan.enumeration.PlanType;
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
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "plans")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class InsurancePlan extends SoftDeletableAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private InsurancePackage insurancePackage;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PlanType type;

    @Column(name = "contribution", nullable = false, precision = 10, scale = 2)
    private BigDecimal contribution;

    @Column(name = "election", nullable = false, precision = 10, scale = 2)
    private BigDecimal election;

    public InsurancePlan(
            InsurancePackage insurancePackage,
            String name,
            PlanType type,
            BigDecimal contribution,
            BigDecimal election
    ) {
        this.insurancePackage = insurancePackage;
        this.name = name;
        this.type = type;
        this.contribution = contribution;
        this.election = election;
    }
}
