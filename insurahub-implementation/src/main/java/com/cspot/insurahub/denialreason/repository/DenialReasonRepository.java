package com.cspot.insurahub.denialreason.repository;

import com.cspot.insurahub.denialreason.entity.DenialReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DenialReasonRepository extends JpaRepository<DenialReason, UUID> {
}
