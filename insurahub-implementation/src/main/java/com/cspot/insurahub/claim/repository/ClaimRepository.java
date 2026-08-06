package com.cspot.insurahub.claim.repository;

import com.cspot.insurahub.claim.entity.Claim;
import com.cspot.insurahub.common.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    default Claim findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Claim.class, id));
    }
}
