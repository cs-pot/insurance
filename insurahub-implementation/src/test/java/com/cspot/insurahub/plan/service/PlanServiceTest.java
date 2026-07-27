package com.cspot.insurahub.plan.service;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.insurancepackage.validation.PackageValidator;
import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.model.PlanResponse;
import com.cspot.insurahub.model.PlanType;
import com.cspot.insurahub.payroll.Payroll;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.mapper.PlanMapper;
import com.cspot.insurahub.plan.repository.InsurancePlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private InsurancePackageRepository insurancePackageRepository;

    @Mock
    private InsurancePlanRepository insurancePlanRepository;

    @Mock
    private PlanMapper planMapper;

    @Mock
    private PackageValidator packageValidator;

    @InjectMocks
    private PlanService planService;

    @Test
    void shouldAddPlanToPackage() {
        UUID packageId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        InsurancePackage insurancePackage = createPackage();
        PlanRequest request = createPlanRequest(
                "Standard Health",
                PlanType.HEALTH_INSURANCE,
                250,
                500
        );
        InsurancePlan plan = createPlan(insurancePackage);
        ReflectionTestUtils.setField(plan, "id", planId);

        when(insurancePackageRepository.findByIdOrThrow(packageId))
                .thenReturn(insurancePackage);
        when(planMapper.toEntity(insurancePackage, request))
                .thenReturn(plan);
        when(insurancePlanRepository.save(plan))
                .thenReturn(plan);

        assertThat(planService.addPlan(packageId, request).getId())
                .isEqualTo(planId);

        verify(insurancePackageRepository).findByIdOrThrow(packageId);
        verify(packageValidator).validateReadyForUpdate(insurancePackage);
        verify(planMapper).toEntity(insurancePackage, request);
        verify(insurancePlanRepository).save(plan);
    }

    @Test
    void shouldAddPlanToInitializedPackage() {
        UUID packageId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        InsurancePackage insurancePackage = createPackage();
        insurancePackage.setStatus(InsurancePackageStatus.INITIALIZED);
        PlanRequest request = createPlanRequest(
                "Dental Basic",
                PlanType.DENTAL_INSURANCE,
                100,
                300
        );
        InsurancePlan plan = createPlan(insurancePackage);
        ReflectionTestUtils.setField(plan, "id", planId);

        when(insurancePackageRepository.findByIdOrThrow(packageId))
                .thenReturn(insurancePackage);
        when(planMapper.toEntity(insurancePackage, request))
                .thenReturn(plan);
        when(insurancePlanRepository.save(plan))
                .thenReturn(plan);

        assertThat(planService.addPlan(packageId, request).getId())
                .isEqualTo(planId);

        verify(packageValidator).validateReadyForUpdate(insurancePackage);
        verify(planMapper).toEntity(insurancePackage, request);
        verify(insurancePlanRepository).save(plan);
    }

    @Test
    void shouldThrowOnAddPlanWhenPackageDoesNotExist() {
        UUID packageId = UUID.randomUUID();
        PlanRequest request = createPlanRequest(
                "Standard Health",
                PlanType.HEALTH_INSURANCE,
                250,
                500
        );

        when(insurancePackageRepository.findByIdOrThrow(packageId))
                .thenThrow(new PackageNotFoundException(packageId));

        assertThrows(
                PackageNotFoundException.class,
                () -> planService.addPlan(packageId, request)
        );

        verify(insurancePackageRepository).findByIdOrThrow(packageId);
        verify(packageValidator, never())
                .validateReadyForUpdate(any(InsurancePackage.class));
        verify(insurancePlanRepository, never()).save(any(InsurancePlan.class));
    }

    @Test
    void shouldReturnPlansOfPackage() {
        UUID packageId = UUID.randomUUID();
        InsurancePackage insurancePackage = createPackage();
        InsurancePlan plan = createPlan(insurancePackage);
        insurancePackage.getPlans().add(plan);
        Pageable pageable = PageRequest.of(0, 10);
        Page<PlanResponse> expectedResponses = new PageImpl<>(
                List.of(
                        new PlanResponse(
                                UUID.randomUUID(),
                                "Standard Health",
                                PlanType.HEALTH_INSURANCE,
                                250,
                                500
                        )
                ),
                pageable,
                1
        );

        when(insurancePackageRepository.findByIdOrThrow(packageId))
                .thenReturn(insurancePackage);
        when(insurancePlanRepository.findByInsurancePackageId(packageId, pageable))
                .thenReturn(new PageImpl<>(List.of(plan), pageable, 1));
        when(planMapper.toPlanResponse(plan))
                .thenReturn(expectedResponses.getContent().get(0));

        assertThat(planService.getPackagePlans(packageId, pageable))
                .isEqualTo(expectedResponses);

        verify(insurancePackageRepository).findByIdOrThrow(packageId);
        verify(planMapper).toPlanResponse(plan);
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
            PlanType type,
            int contribution,
            int election
    ) {
        return new PlanRequest(
                name,
                type,
                BigDecimal.valueOf(contribution),
                BigDecimal.valueOf(election)
        );
    }

    private InsurancePlan createPlan(InsurancePackage insurancePackage) {
        return new InsurancePlan(
                insurancePackage,
                "Plan",
                PlanType.HEALTH_INSURANCE,
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(500)
        );
    }
}

