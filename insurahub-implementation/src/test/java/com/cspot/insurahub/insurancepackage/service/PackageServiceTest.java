package com.cspot.insurahub.insurancepackage.service;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.enumeration.InsurancePackageStatus;
import com.cspot.insurahub.insurancepackage.exception.InvalidPackageException;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import com.cspot.insurahub.insurancepackage.mapper.PackageMapper;
import com.cspot.insurahub.insurancepackage.repository.InsurancePackageRepository;
import com.cspot.insurahub.insurancepackage.validation.PackageValidator;
import com.cspot.insurahub.model.PostPackageRequest;
import com.cspot.insurahub.payroll.Payroll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-09T00:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private InsurancePackageRepository insurancePackageRepository;

    @Mock
    private PackageMapper packageMapper;

    private PackageService packageService;

    private PackageValidator packageValidator;

    @BeforeEach
    void setUp() {
        packageValidator = new PackageValidator(CLOCK);

        packageService = new PackageService(
                insurancePackageRepository,
                packageMapper,
                packageValidator
        );
    }

    @Test
    void shouldCreatePackage() {
        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 9);

        PostPackageRequest request = new PostPackageRequest(
                "Premium Health Package",
                PostPackageRequest.PayrollEnum.MONTHLY,
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
    }

    @Test
    void shouldRejectStartDateBeforeToday() {
        LocalDate startDate = LocalDate.of(2026, 7, 8);
        LocalDate endDate = LocalDate.of(2026, 8, 8);

        PostPackageRequest request = new PostPackageRequest(
                "Premium Health Package",
                PostPackageRequest.PayrollEnum.MONTHLY,
                startDate,
                endDate
        );

        InvalidPackageException exception = assertThrows(
                InvalidPackageException.class,
                () -> packageService.createPackage(request)
        );

        assertThat(exception.getCode())
                .isEqualTo("PACKAGE_START_DATE_IN_PAST");

        verify(packageMapper, never())
                .initializeFromCreateRequest(any(PostPackageRequest.class));

        verify(insurancePackageRepository, never())
                .save(any(InsurancePackage.class));
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
        when(insurancePackageRepository.findById(packageId)).thenReturn(Optional.of(insurancePackage));

        packageService.initializePackage(packageId);

        assertThat(insurancePackage.getStatus())
                .isEqualTo(InsurancePackageStatus.INITIALIZED);
        verify(insurancePackageRepository).findById(packageId);
    }

    @Test
    void shouldRejectAlreadyInitializedPackage() {
        UUID packageId = UUID.randomUUID();
        InsurancePackage insurancePackage = new InsurancePackage(
                "Premium Health Package",
                Payroll.MONTHLY,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 9)
        );
        ReflectionTestUtils.setField(
                insurancePackage,
                "status",
                InsurancePackageStatus.INITIALIZED
        );
        when(insurancePackageRepository.findById(packageId)).thenReturn(Optional.of(insurancePackage));

        InvalidPackageException exception = assertThrows(
                InvalidPackageException.class,
                () -> packageService.initializePackage(packageId)
        );

        assertThat(exception.getCode())
                .isEqualTo("PACKAGE_ALREADY_INITIALIZED");
        assertThat(insurancePackage.getStatus())
                .isEqualTo(InsurancePackageStatus.INITIALIZED);
        verify(insurancePackageRepository).findById(packageId);
    }

    @Test
    void shouldRejectInitializePackageWhenPackageDoesNotExist() {
        UUID packageId = UUID.randomUUID();
        when(insurancePackageRepository.findById(packageId)).thenReturn(Optional.empty());

        assertThrows(
                PackageNotFoundException.class,
                () -> packageService.initializePackage(packageId)
        );

        verify(insurancePackageRepository).findById(packageId);
    }
}
