package com.cspot.insurahub.plan.entity;

import com.cspot.insurahub.common.SoftDeletableAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan extends SoftDeletableAuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "election_amount", nullable = false)
    private BigDecimal electionAmount;

    @Column(name = "contribution_amount", nullable = false)
    private BigDecimal contributionAmount;

    @Column(name = "coverage_details")
    private String coverageDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PlanStatus status;
}