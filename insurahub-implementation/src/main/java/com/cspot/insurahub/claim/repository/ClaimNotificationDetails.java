package com.cspot.insurahub.claim.repository;

import com.cspot.insurahub.claim.enumeration.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ClaimNotificationDetails {

    String getConsumerEmail();

    String getClaimNumber();

    String getPlanName();

    LocalDate getServiceDate();

    BigDecimal getAmount();

    ClaimStatus getStatus();

    String getDenialReasonTitle();

    String getDenialReasonDescription();
}
