package com.cspot.insurahub.notification.service;

import com.cspot.insurahub.claim.repository.ClaimNotificationDetails;
import com.cspot.insurahub.claim.repository.ClaimRepository;
import com.cspot.insurahub.common.exception.ResourceNotFoundException;
import com.cspot.insurahub.notification.distributor.EmailDistributor;
import com.cspot.insurahub.notification.renderer.PlainTextEmailRenderer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    public static final String CLAIM_APPROVAL_TITLE = "Your claim has been approved";
    public static final String CLAIM_DENIAL_TITLE = "Your claim has been denied";

    private final EmailDistributor emailDistributor;
    private final PlainTextEmailRenderer emailRenderer;
    private final ClaimRepository claimRepository;

    public void sendClaimApprovalNotification(String claimNumber) {
        String content = emailRenderer.render(
                "ClaimApproved",
                Map.of("claimNumber", claimNumber)
        );
        String receiverAddress = getReceiverAddressForClaimModification(claimNumber);
        emailDistributor.sendEmail(receiverAddress, CLAIM_APPROVAL_TITLE, content);
    }

    public void sendClaimDenialNotification(String claimNumber) {
        ClaimNotificationDetails details = claimRepository.findClaimNotificationDetails(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim with number " + claimNumber + " not found"));

        String content = emailRenderer.render(
                "ClaimDenied",
                Map.of(
                        "claimNumber", details.getClaimNumber(),
                        "planName", details.getPlanName(),
                        "serviceDate", details.getServiceDate(),
                        "claimAmount", details.getAmount(),
                        "claimStatus", toDisplayValue(details.getStatus().name()),
                        "denialReasonBlock", formatDenialReasonBlock(details)
                )
        );
        emailDistributor.sendEmail(details.getConsumerEmail(), CLAIM_DENIAL_TITLE, content);
    }

    private String formatDenialReasonBlock(ClaimNotificationDetails details) {
        String denialReason = details.getDenialReasonTitle();
        if (denialReason == null || denialReason.isBlank()) {
            return "";
        }

        String description = details.getDenialReasonDescription();
        if (description != null && !description.isBlank()) {
            denialReason += ": " + description;
        }

        return System.lineSeparator() + "Denial Reason: " + denialReason;
    }

    private String toDisplayValue(String value) {
        return value.charAt(0) + value.substring(1).toLowerCase();
    }

    private @NonNull String getReceiverAddressForClaimModification(String claimNumber) {
        String receiverAddress = claimRepository.findClaimConsumerEmail(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim with number " + claimNumber + " not found"));
        return receiverAddress;
    }
}
