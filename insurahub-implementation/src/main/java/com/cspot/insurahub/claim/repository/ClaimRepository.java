package com.cspot.insurahub.claim.repository;

import com.cspot.insurahub.claim.entity.Claim;
import com.cspot.insurahub.common.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID>, JpaSpecificationExecutor<Claim> {

    @Query("""
            SELECT c.enrollment.consumer.email
            FROM Claim c
            WHERE c.claimNumber = :claimNumber
            """)
    Optional<String> findClaimConsumerEmail(@Param("claimNumber") String claimNumber);

    default Claim findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Claim.class, id));
    }
}
