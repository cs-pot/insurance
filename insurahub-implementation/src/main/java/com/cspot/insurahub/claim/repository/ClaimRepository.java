package com.cspot.insurahub.claim.repository;

import com.cspot.insurahub.claim.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    @Query(value = "SELECT c FROM Claim c JOIN FETCH c.employee JOIN FETCH c.plan",
            countQuery = "SELECT count(c) FROM Claim c")
    Page<Claim> findAllWithDetails(Pageable pageable);
}