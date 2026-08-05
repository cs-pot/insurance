package com.cspot.insurahub.insurancepackage.service;

import com.cspot.insurahub.auth.service.AuthenticationMetadataQueryService;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.insurancepackage.filter.PackageFilter;
import com.cspot.insurahub.insurancepackage.mapper.PackageMapper;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.insurancepackage.validation.PackageValidator;
import com.cspot.insurahub.model.PackageRequest;
import com.cspot.insurahub.model.PackageResponse;
import com.cspot.insurahub.model.PlanType;
import com.cspot.insurahub.payroll.Payroll;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

    @Mock
    private InsurancePackageRepository insurancePackageRepository;

    @Mock
    private PackageMapper packageMapper;

    @Mock
    private AuthenticationMetadataQueryService authenticationMetadataQueryService;

    @Mock
    private PackageValidator packageValidator;

    @InjectMocks
    private PackageService packageService;

    @Test
    void shouldGetPackagesFilteredByName() {
        Pageable pageable = PageRequest.of(0, 20);
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );
        PackageResponse response = new PackageResponse(
                UUID.randomUUID(),
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );

        when(insurancePackageRepository.findAll(
                anyPackageSpecification(),
                same(pageable)
        ))
                .thenReturn(new PageImpl<>(List.of(insurancePackage), pageable, 1));
        when(packageMapper.toListItemResponse(insurancePackage))
                .thenReturn(response);

        assertThat(packageService.getPackages(new PackageFilter("  premium  "), pageable).getContent())
                .containsExactly(response);

        verify(insurancePackageRepository)
                .findAll(anyPackageSpecification(), same(pageable));
    }

    @Test
    void shouldGetAllPackagesWhenNameIsBlank() {
        Pageable pageable = PageRequest.of(0, 20);
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );
        PackageResponse response = new PackageResponse(
                UUID.randomUUID(),
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );

        when(insurancePackageRepository.findAll(
                anyPackageSpecification(),
                same(pageable)
        ))
                .thenReturn(new PageImpl<>(List.of(insurancePackage), pageable, 1));
        when(packageMapper.toListItemResponse(insurancePackage))
                .thenReturn(response);

        assertThat(packageService.getPackages(new PackageFilter("   "), pageable).getContent())
                .containsExactly(response);

        verify(insurancePackageRepository)
                .findAll(anyPackageSpecification(), same(pageable));
    }

    @Test
    void shouldCreatePackage() {
        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 9);

        PackageRequest request = new PackageRequest(
                "Premium Health Package",
                Payroll.MONTHLY,
                startDate,
                endDate
        );

        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                startDate,
                endDate
        );

        UUID packageId = UUID.randomUUID();

        ReflectionTestUtils.setField(
                insurancePackage,
                "id",
                packageId
        );

        when(packageMapper.initializeFromCreateRequest(request))
                .thenReturn(insurancePackage);
        when(insurancePackageRepository.save(insurancePackage))
                .thenReturn(insurancePackage);

        assertThat(packageService.createPackage(request).getId())
                .isEqualTo(packageId);

        verify(packageMapper)
                .initializeFromCreateRequest(request);
        verify(insurancePackageRepository)
                .save(insurancePackage);
        verify(packageValidator).validate(request);
    }

    @Test
    void shouldUpdatePackage() {
        UUID packageId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 10);

        PackageRequest request = new PackageRequest(
                "Updated Package",
                Payroll.MONTHLY,
                startDate,
                endDate
        );

        InsurancePackage insurancePackage = new InsurancePackage(
                "Original Package",
                Payroll.WEEKLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 17)
        );
        ReflectionTestUtils.setField(insurancePackage, "id", packageId);

        when(insurancePackageRepository.findByIdOrThrow(packageId))
                .thenReturn(insurancePackage);
        doAnswer(invocation -> {
            InsurancePackage target = invocation.getArgument(0);
            target.setName("Updated Package");
            target.setPayroll(Payroll.MONTHLY);
            target.setStartDate(startDate);
            target.setEndDate(endDate);
            return null;
        }).when(packageMapper).updateFromUpdateRequest(same(insurancePackage), same(request));

        packageService.updatePackage(packageId, request);

        InOrder inOrder = inOrder(insurancePackageRepository, packageMapper, packageValidator);
        inOrder.verify(insurancePackageRepository).findByIdOrThrow(packageId);
        inOrder.verify(packageValidator).validateReadyForUpdate(insurancePackage);
        inOrder.verify(packageMapper).updateFromUpdateRequest(insurancePackage, request);
        inOrder.verify(packageValidator).validate(insurancePackage);
        verifyNoMoreInteractions(insurancePackageRepository, packageMapper);
    }

    @Test
    void shouldThrowOnUpdateWhenPackageNotFound() {
        UUID packageId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 10);

        PackageRequest request = new PackageRequest(
                "Updated Package",
                Payroll.MONTHLY,
                startDate,
                endDate
        );

        when(insurancePackageRepository.findByIdOrThrow(packageId))
                .thenThrow(new PackageNotFoundException(packageId));

        assertThrows(
                PackageNotFoundException.class,
                () -> packageService.updatePackage(packageId, request)
        );

        verify(insurancePackageRepository).findByIdOrThrow(packageId);
        verify(packageMapper, never()).updateFromUpdateRequest(
                any(InsurancePackage.class),
                any(PackageRequest.class)
        );
        verifyNoMoreInteractions(insurancePackageRepository, packageMapper);
    }

    @Test
    void shouldInitializePackage() {
        UUID packageId = UUID.randomUUID();
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );
        when(insurancePackageRepository.findByIdOrThrow(packageId))
                .thenReturn(insurancePackage);

        packageService.initializePackage(packageId);

        assertThat(insurancePackage.getStatus())
                .isEqualTo(InsurancePackageStatus.INITIALIZED);
        verify(insurancePackageRepository).findByIdOrThrow(packageId);
        verify(packageValidator).validateReadyForInitialization(insurancePackage);
    }

    @Test
    void shouldRejectInitializePackageWhenPackageDoesNotExist() {
        UUID packageId = UUID.randomUUID();
        when(insurancePackageRepository.findByIdOrThrow(packageId))
                .thenThrow(new PackageNotFoundException(packageId));

        assertThrows(
                PackageNotFoundException.class,
                () -> packageService.initializePackage(packageId)
        );

        verify(insurancePackageRepository).findByIdOrThrow(packageId);
    }

    @Test
    void shouldRemovePackageAndConnectedPlans() {
        UUID packageId = UUID.randomUUID();
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );
        InsurancePlan plan = new InsurancePlan(
                insurancePackage,
                "Standard Health",
                PlanType.HEALTH_INSURANCE,
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(500)
        );
        insurancePackage.getPlans().add(plan);

        when(insurancePackageRepository.findByIdOrThrow(packageId))
                .thenReturn(insurancePackage);
        mockAuthenticatedPrincipalName();

        packageService.deletePackage(packageId);

        assertThat(insurancePackage.isDeleted()).isTrue();
        assertThat(insurancePackage.getDeletedBy()).isEqualTo("admin-user");
        assertThat(plan.isDeleted()).isTrue();
        assertThat(plan.getDeletedBy()).isEqualTo("admin-user");
        verify(packageValidator).validateReadyForRemoval(insurancePackage);
    }

    private void mockAuthenticatedPrincipalName() {
        when(authenticationMetadataQueryService.getRequiredAuthenticatedPrincipalName())
                .thenReturn("admin-user");
    }

    private Specification<InsurancePackage> anyPackageSpecification() {
        return any();
    }
}
