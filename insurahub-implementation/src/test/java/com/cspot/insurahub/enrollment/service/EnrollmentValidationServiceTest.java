package com.cspot.insurahub.enrollment.service;

import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.enrollment.exception.EnrollmentDeniedException;
import com.cspot.insurahub.enrollment.repository.EnrollmentRepository;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.model.PlanType;
import com.cspot.insurahub.payroll.Payroll;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentValidationServiceTest {

    private static UUID CONSUMER_ID = UUID.randomUUID();
    private static UUID PLAN_ID = UUID.randomUUID();

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentValidationService service;

    @Test
    void shouldAllowEnrollmentWhenPlanIsInitializedAndConsumerNotAlreadyEnrolled() {
        Consumer consumer = consumer(CONSUMER_ID);
        InsurancePlan plan = plan(PLAN_ID, InsurancePackageStatus.INITIALIZED);

        when(enrollmentRepository.existsByConsumerIdAndPlanId(CONSUMER_ID, PLAN_ID))
                .thenReturn(false);

        assertDoesNotThrow(() ->
                service.assertConsumerCanEnrollOnPlan(consumer, plan));

        verify(enrollmentRepository)
                .existsByConsumerIdAndPlanId(CONSUMER_ID, PLAN_ID);
    }

    @Test
    void shouldThrowWhenPlanPackageIsNotInitialized() {
        Consumer consumer = consumer(CONSUMER_ID);
        InsurancePlan plan = plan(PLAN_ID, InsurancePackageStatus.NOT_STARTED);

        EnrollmentDeniedException exception = assertThrows(
                EnrollmentDeniedException.class,
                () -> service.assertConsumerCanEnrollOnPlan(consumer, plan)
        );

        assertEquals(
                "Only packages with status INITIALIZED can be enrolled on",
                exception.getMessage()
        );

        verifyNoInteractions(enrollmentRepository);
    }

    @Test
    void shouldThrowWhenConsumerAlreadyEnrolled() {
        Consumer consumer = consumer(CONSUMER_ID);
        InsurancePlan plan = plan(PLAN_ID, InsurancePackageStatus.INITIALIZED);

        when(enrollmentRepository.existsByConsumerIdAndPlanId(CONSUMER_ID, PLAN_ID))
                .thenReturn(true);

        EnrollmentDeniedException exception = assertThrows(
                EnrollmentDeniedException.class,
                () -> service.assertConsumerCanEnrollOnPlan(consumer, plan)
        );

        assertEquals(
                "You are already enrolled on this plan",
                exception.getMessage()
        );

        verify(enrollmentRepository)
                .existsByConsumerIdAndPlanId(CONSUMER_ID, PLAN_ID);
    }

    private Consumer consumer(UUID id) {
        Consumer consumer = new Consumer();
        ReflectionTestUtils.setField(consumer, "id", id);
        return consumer;
    }

    private InsurancePlan plan(UUID id, InsurancePackageStatus status) {
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );
        insurancePackage.setStatus(status);

        InsurancePlan plan = new InsurancePlan(
                insurancePackage,
                "Plan",
                PlanType.HEALTH_INSURANCE,
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(500)
        );
        ReflectionTestUtils.setField(plan, "id", id);
        plan.setInsurancePackage(insurancePackage);

        return plan;
    }
}
