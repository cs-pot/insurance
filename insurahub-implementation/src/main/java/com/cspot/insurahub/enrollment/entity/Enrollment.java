package com.cspot.insurahub.enrollment.entity;

import com.cspot.insurahub.common.SoftDeletableAuditableEntity;
import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends SoftDeletableAuditableEntity {

    @ManyToOne
    @JoinColumn(name = "consumer_id", nullable = false)
    private Consumer consumer;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private InsurancePlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status;

    public Enrollment(Consumer consumer, InsurancePlan insurancePlan) {
        this(consumer, insurancePlan, EnrollmentStatus.ACTIVE);
    }

    public Enrollment(
            Consumer consumer,
            InsurancePlan plan,
            EnrollmentStatus status
    ) {
        this.consumer = consumer;
        this.plan = plan;
        this.status = status;
    }
}
