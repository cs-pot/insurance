package com.cspot.insurahub.claim.repository;

import com.cspot.insurahub.claim.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {

    Optional<Receipt> findByClaimId(UUID claimId);
}
