package com.cspot.insurahub.insurancepackage;

import com.cspot.insurahub.common.SoftDeletableAuditableEntity;
import com.cspot.insurahub.payroll.Payroll;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Entity
@Table(name = "packages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InsurancePackage extends SoftDeletableAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll", nullable = false)
    private Payroll payroll;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public InsurancePackage(
            String name,
            Payroll payroll,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.name = name;
        this.payroll = payroll;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
