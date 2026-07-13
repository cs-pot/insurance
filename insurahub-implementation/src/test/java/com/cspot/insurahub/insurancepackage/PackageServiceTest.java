package com.cspot.insurahub.insurancepackage;

import com.cspot.insurahub.insurancepackage.converter.PackageMapper;
import com.cspot.insurahub.model.PackageCreateRequest;
import com.cspot.insurahub.payroll.Payroll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void shouldCreatePackage() {
        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 9);
        PackageCreateRequest packageCreateRequest = new PackageCreateRequest(
                "Premium Health Package",
                PackageCreateRequest.PayrollEnum.MONTHLY,
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
        ReflectionTestUtils.setField(insurancePackage, "id", packageId);
        PackageService packageService = new PackageService(
                insurancePackageRepository,
                packageMapper,
                CLOCK
        );
        when(packageMapper.initializeFromCreateRequest(packageCreateRequest)).thenReturn(insurancePackage);
        when(insurancePackageRepository.save(insurancePackage)).thenReturn(insurancePackage);

        UUID createdPackageId = packageService.createPackage(packageCreateRequest);

        assertEquals(packageId, createdPackageId);
        verify(packageMapper).initializeFromCreateRequest(packageCreateRequest);
        verify(insurancePackageRepository).save(insurancePackage);
    }

    @Test
    void shouldRejectStartDateBeforeToday() {
        PackageCreateRequest packageCreateRequest = new PackageCreateRequest(
                "Premium Health Package",
                PackageCreateRequest.PayrollEnum.MONTHLY,
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 8, 8)
        );
        PackageService packageService = new PackageService(
                insurancePackageRepository,
                packageMapper,
                CLOCK
        );

        InvalidPackageException exception = assertThrows(
                InvalidPackageException.class,
                () -> packageService.createPackage(packageCreateRequest)
        );

        assertEquals("PACKAGE_START_DATE_IN_PAST", exception.getCode());
        verify(packageMapper, never()).initializeFromCreateRequest(any(PackageCreateRequest.class));
        verify(insurancePackageRepository, never()).save(any(InsurancePackage.class));
    }
}
