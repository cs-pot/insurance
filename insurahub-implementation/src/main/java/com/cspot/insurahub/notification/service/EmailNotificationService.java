package com.cspot.insurahub.notification.service;

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

    private @NonNull String getReceiverAddressForClaimModification(String claimNumber) {
        String receiverAddress = claimRepository.findClaimConsumerEmail(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim with number " + claimNumber + " not found"));
        return receiverAddress;
    }
}
