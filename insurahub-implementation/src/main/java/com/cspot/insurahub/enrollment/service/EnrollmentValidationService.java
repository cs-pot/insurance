package com.cspot.insurahub.enrollment.service;

import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.enrollment.exception.EnrollmentDeniedException;
import com.cspot.insurahub.enrollment.repository.EnrollmentRepository;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentValidationService {

    private final EnrollmentRepository enrollmentRepository;

    public void assertConsumerCanEnrollOnPlan(Consumer consumer, InsurancePlan insurancePlan) {
        assertPlanCanBeEnrolledOn(insurancePlan);
        assertConsumerIsNotAlreadyEnrolled(consumer, insurancePlan);
    }

    private void assertConsumerIsNotAlreadyEnrolled(Consumer consumer, InsurancePlan insurancePlan) {
        if (enrollmentRepository.existsByConsumerIdAndPlanId(consumer.getId(), insurancePlan.getId())) {
            throw new EnrollmentDeniedException("You are already enrolled on this plan");
        }
    }

    private void assertPlanCanBeEnrolledOn(InsurancePlan insurancePlan) {
        if (insurancePlan.getInsurancePackage().getStatus() != InsurancePackageStatus.INITIALIZED) {
            throw new EnrollmentDeniedException("Only packages with status INITIALIZED can be enrolled on");
        }
    }
}
