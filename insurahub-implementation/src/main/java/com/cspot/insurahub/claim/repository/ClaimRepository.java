package com.cspot.insurahub.claim.repository;

import com.cspot.insurahub.claim.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    @Query("SELECT c FROM Claim c JOIN FETCH c.enrollment e JOIN FETCH e.consumer JOIN FETCH e.plan")
    Page<Claim> findAllWithDetails(Pageable pageable);
}
