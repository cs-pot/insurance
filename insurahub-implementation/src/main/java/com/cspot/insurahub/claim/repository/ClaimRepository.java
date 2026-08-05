package com.cspot.insurahub.claim.repository;

import com.cspot.insurahub.claim.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

}
