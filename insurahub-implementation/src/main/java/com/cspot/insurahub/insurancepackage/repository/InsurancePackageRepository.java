package com.cspot.insurahub.insurancepackage.repository;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InsurancePackageRepository extends JpaRepository<InsurancePackage, UUID> {

}
