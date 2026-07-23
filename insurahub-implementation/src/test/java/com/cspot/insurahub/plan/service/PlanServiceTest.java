package com.cspot.insurahub.plan.service;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.insurancepackage.exception.PackageUpdateNotAllowedException;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.insurancepackage.validation.PackageValidator;
import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.payroll.Payroll;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.exception.InvalidPlanException;
import com.cspot.insurahub.plan.mapper.PlanMapper;
import com.cspot.insurahub.plan.repository.InsurancePlanRepository;
import com.cspot.insurahub.plan.validation.PlanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-09T00:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private InsurancePackageRepository insurancePackageRepository;

    @Mock
    private InsurancePlanRepository insurancePlanRepository;

    @Mock
    private PlanMapper planMapper;

    @Mock
    private PlanValidator planValidator;

    private PlanService planService;
    private PackageValidator packageValidator;

    @BeforeEach
    void setUp() {
        packageValidator = new PackageValidator(CLOCK);
        planService = new PlanService(
                insurancePackageRepository,
                insurancePlanRepository,
                planMapper,
                planValidator,
                packageValidator
        );
    }

    @Test
    void shouldAddPlanToPackage() {
        UUID packageId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        InsurancePackage insurancePackage = createPackage();
        PlanRequest request = createPlanRequest(
                "Standard Health",
                com.cspot.insurahub.model.PlanType.HEALTH_INSURANCE,
                250,
                500
        );
        InsurancePlan plan = createPlan(
                insurancePackage,
                "Standard Health",
                com.cspot.insurahub.plan.enumeration.PlanType.HEALTH_INSURANCE,
                250,
                500
        );
        ReflectionTestUtils.setField(plan, "id", planId);

        when(insurancePackageRepository.findById(packageId))
                .thenReturn(Optional.of(insurancePackage));
        when(planMapper.toEntity(insurancePackage, request))
                .thenReturn(plan);
        when(insurancePlanRepository.save(plan))
                .thenReturn(plan);

        assertThat(planService.addPlan(packageId, request).getId())
                .isEqualTo(planId);

        verify(insurancePackageRepository).findById(packageId);
        verify(planValidator).validate(request);
        verify(planMapper).toEntity(insurancePackage, request);
        verify(insurancePlanRepository).save(plan);
    }

    @Test
    void shouldRejectAddingPlanToInitializedPackage() {
        UUID packageId = UUID.randomUUID();
        InsurancePackage insurancePackage = createPackage();
        insurancePackage.setStatus(InsurancePackageStatus.INITIALIZED);
        PlanRequest request = createPlanRequest(
                "Dental Basic",
                com.cspot.insurahub.model.PlanType.DENTAL_INSURANCE,
                100,
                300
        );
        when(insurancePackageRepository.findById(packageId))
                .thenReturn(Optional.of(insurancePackage));

        assertThrows(
                PackageUpdateNotAllowedException.class,
                () -> planService.addPlan(packageId, request)
        );

        verify(planMapper, never()).toEntity(
                any(InsurancePackage.class),
                any(PlanRequest.class)
        );
        verify(planValidator, never()).validate(any(PlanRequest.class));
        verify(insurancePlanRepository, never())
                .save(any(InsurancePlan.class));
    }

    @Test
    void shouldThrowOnAddPlanWhenPackageDoesNotExist() {
        UUID packageId = UUID.randomUUID();
        PlanRequest request = createPlanRequest(
                "Standard Health",
                com.cspot.insurahub.model.PlanType.HEALTH_INSURANCE,
                250,
                500
        );

        when(insurancePackageRepository.findById(packageId))
                .thenReturn(Optional.empty());

        assertThrows(
                PackageNotFoundException.class,
                () -> planService.addPlan(packageId, request)
        );

        verify(insurancePackageRepository).findById(packageId);
        verify(planValidator, never()).validate(any(PlanRequest.class));
        verify(insurancePlanRepository, never()).save(any(InsurancePlan.class));
    }

    @Test
    void shouldThrowWhenPlanValidationFails() {
        UUID packageId = UUID.randomUUID();
        InsurancePackage insurancePackage = createPackage();
        PlanRequest request = createPlanRequest(
                "Plan!",
                com.cspot.insurahub.model.PlanType.HEALTH_INSURANCE,
                250,
                500
        );

        when(insurancePackageRepository.findById(packageId))
                .thenReturn(Optional.of(insurancePackage));
        doThrow(new InvalidPlanException(
                "PLAN_NAME_INVALID",
                "Plan name must contain only English letters and spaces, up to 50 characters"
        )).when(planValidator).validate(request);

        InvalidPlanException exception = assertThrows(
                InvalidPlanException.class,
                () -> planService.addPlan(packageId, request)
        );

        assertThat(exception.getCode())
                .isEqualTo("PLAN_NAME_INVALID");
        verify(planValidator).validate(request);
        verify(planMapper, never()).toEntity(
                any(InsurancePackage.class),
                any(PlanRequest.class)
        );
        verify(insurancePlanRepository, never()).save(any(InsurancePlan.class));
    }

    private InsurancePackage createPackage() {
        return new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );
    }

    private PlanRequest createPlanRequest(
            String name,
            com.cspot.insurahub.model.PlanType type,
            int contribution,
            int election
    ) {
        return new PlanRequest(
                name,
                type,
                contribution,
                election
        );
    }

    private InsurancePlan createPlan(
            InsurancePackage insurancePackage,
            String name,
            com.cspot.insurahub.plan.enumeration.PlanType type,
            int contribution,
            int election
    ) {
        return new InsurancePlan(
                insurancePackage,
                name,
                type,
                BigDecimal.valueOf(contribution),
                BigDecimal.valueOf(election)
        );
    }
}
