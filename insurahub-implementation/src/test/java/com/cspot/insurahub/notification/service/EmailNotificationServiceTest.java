package com.cspot.insurahub.notification.service;

import com.cspot.insurahub.claim.enumeration.ClaimStatus;
import com.cspot.insurahub.claim.repository.ClaimNotificationDetails;
import com.cspot.insurahub.claim.repository.ClaimRepository;
import com.cspot.insurahub.common.exception.ResourceNotFoundException;
import com.cspot.insurahub.notification.distributor.EmailDistributor;
import com.cspot.insurahub.notification.renderer.PlainTextEmailRenderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    private static final String CLAIM_NUMBER = "CLM-123";
    private static final String RECEIVER_EMAIL = "consumer@example.com";
    private static final String RENDERED_CONTENT = "Your claim CLM-123 has been approved";
    private static final String RENDERED_DENIAL_CONTENT = "Your claim CLM-123 has been denied";

    @Mock
    private EmailDistributor emailDistributor;

    @Mock
    private PlainTextEmailRenderer emailRenderer;

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @Test
    void shouldSendClaimApprovalNotification() {
        when(emailRenderer.render(
                "ClaimApproved",
                Map.of("claimNumber", CLAIM_NUMBER)
        )).thenReturn(RENDERED_CONTENT);

        when(claimRepository.findClaimConsumerEmail(CLAIM_NUMBER))
                .thenReturn(Optional.of(RECEIVER_EMAIL));

        emailNotificationService.sendClaimApprovalNotification(CLAIM_NUMBER);

        verify(emailRenderer).render(
                "ClaimApproved",
                Map.of("claimNumber", CLAIM_NUMBER)
        );
        verify(claimRepository).findClaimConsumerEmail(CLAIM_NUMBER);
        verify(emailDistributor).sendEmail(
                RECEIVER_EMAIL,
                EmailNotificationService.CLAIM_APPROVAL_TITLE,
                RENDERED_CONTENT
        );
    }

    @Test
    void shouldSendClaimDenialNotification() {
        ClaimNotificationDetails details = claimNotificationDetails(
                "Service is not covered by your plan"
        );

        when(claimRepository.findClaimNotificationDetails(CLAIM_NUMBER))
                .thenReturn(Optional.of(details));
        when(emailRenderer.render(
                "ClaimDenied",
                Map.of(
                        "claimNumber", CLAIM_NUMBER,
                        "planName", "Standard Health",
                        "serviceDate", LocalDate.of(2026, 7, 10),
                        "claimAmount", new BigDecimal("123.45"),
                        "claimStatus", "Denied",
                        "denialReasonBlock",
                        System.lineSeparator() + "Denial Reason: Eligibility: Service is not covered by your plan"
                )
        )).thenReturn(RENDERED_DENIAL_CONTENT);

        emailNotificationService.sendClaimDenialNotification(CLAIM_NUMBER);

        verify(claimRepository).findClaimNotificationDetails(CLAIM_NUMBER);
        verify(emailRenderer).render(
                "ClaimDenied",
                Map.of(
                        "claimNumber", CLAIM_NUMBER,
                        "planName", "Standard Health",
                        "serviceDate", LocalDate.of(2026, 7, 10),
                        "claimAmount", new BigDecimal("123.45"),
                        "claimStatus", "Denied",
                        "denialReasonBlock",
                        System.lineSeparator() + "Denial Reason: Eligibility: Service is not covered by your plan"
                )
        );
        verify(emailDistributor).sendEmail(
                RECEIVER_EMAIL,
                EmailNotificationService.CLAIM_DENIAL_TITLE,
                RENDERED_DENIAL_CONTENT
        );
    }

    @Test
    void shouldThrowWhenDenialReasonIsNotAvailable() {
        ClaimNotificationDetails details = claimNotificationDetails("");

        when(claimRepository.findClaimNotificationDetails(CLAIM_NUMBER))
                .thenReturn(Optional.of(details));

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> emailNotificationService.sendClaimDenialNotification(CLAIM_NUMBER)
        );

        Assertions.assertEquals(
                "Denial reason is required for denied claim " + CLAIM_NUMBER,
                exception.getMessage()
        );
        verifyNoInteractions(emailRenderer);
        verifyNoInteractions(emailDistributor);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenClaimDoesNotExist() {
        when(claimRepository.findClaimConsumerEmail(CLAIM_NUMBER))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> emailNotificationService.sendClaimApprovalNotification(CLAIM_NUMBER)
        );

        Assertions.assertEquals(
                "Claim with number " + CLAIM_NUMBER + " not found",
                exception.getMessage()
        );

        verify(claimRepository).findClaimConsumerEmail(CLAIM_NUMBER);
        verify(emailDistributor, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void shouldNotSendEmailWhenReceiverEmailCannotBeFound() {
        when(claimRepository.findClaimConsumerEmail(CLAIM_NUMBER))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> emailNotificationService.sendClaimApprovalNotification(CLAIM_NUMBER)
        );

        verifyNoInteractions(emailDistributor);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenClaimDetailsCannotBeFound() {
        when(claimRepository.findClaimNotificationDetails(CLAIM_NUMBER))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> emailNotificationService.sendClaimDenialNotification(CLAIM_NUMBER)
        );

        Assertions.assertEquals(
                "Claim with number " + CLAIM_NUMBER + " not found",
                exception.getMessage()
        );

        verify(claimRepository).findClaimNotificationDetails(CLAIM_NUMBER);
        verifyNoInteractions(emailDistributor);
    }

    private ClaimNotificationDetails claimNotificationDetails(String denialReasonDescription) {
        return new ClaimNotificationDetails() {

            @Override
            public String getConsumerEmail() {
                return RECEIVER_EMAIL;
            }

            @Override
            public String getClaimNumber() {
                return CLAIM_NUMBER;
            }

            @Override
            public String getPlanName() {
                return "Standard Health";
            }

            @Override
            public LocalDate getServiceDate() {
                return LocalDate.of(2026, 7, 10);
            }

            @Override
            public BigDecimal getAmount() {
                return new BigDecimal("123.45");
            }

            @Override
            public ClaimStatus getStatus() {
                return ClaimStatus.DENIED;
            }

            @Override
            public String getDenialReasonTitle() {
                return denialReasonDescription.isBlank() ? null : "Eligibility";
            }

            @Override
            public String getDenialReasonDescription() {
                return denialReasonDescription;
            }
        };
    }
}
