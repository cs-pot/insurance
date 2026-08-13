package com.cspot.insurahub.notification.service;

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
}