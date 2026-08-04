package com.cspot.insurahub.insurancepackage.repository;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.insurancepackage.exception.PackageNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InsurancePackageRepository extends JpaRepository<InsurancePackage, UUID> {

    Page<InsurancePackage> findByNameContainingIgnoreCase(String name, Pageable pageable);

    default InsurancePackage findByIdOrThrow(UUID packageId) {
        return findById(packageId)
                .orElseThrow(() -> new PackageNotFoundException(packageId));
    }
}
