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

    @Query("""
            SELECT
                c.enrollment.consumer.email AS consumerEmail,
                c.claimNumber AS claimNumber,
                c.enrollment.plan.name AS planName,
                c.serviceDate AS serviceDate,
                c.amount AS amount,
                c.status AS status,
                denialReason.title AS denialReasonTitle,
                denialReason.description AS denialReasonDescription
            FROM Claim c
            LEFT JOIN c.denialReason denialReason
            WHERE c.claimNumber = :claimNumber
            """)
    Optional<ClaimNotificationDetails> findClaimNotificationDetails(@Param("claimNumber") String claimNumber);

    default Claim findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Claim.class, id));
    }
}
