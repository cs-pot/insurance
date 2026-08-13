package com.cspot.insurahub.claim.service;

import com.cspot.insurahub.claim.entity.Claim;
import com.cspot.insurahub.claim.enumeration.ClaimStatus;
import com.cspot.insurahub.claim.repository.ClaimRepository;
import com.cspot.insurahub.claim.mapper.ClaimMapper;
import com.cspot.insurahub.claim.repository.ReceiptRepository;
import com.cspot.insurahub.claim.storage.PostgresReceiptStorage;
import com.cspot.insurahub.common.exception.DomainValidationException;
import com.cspot.insurahub.common.exception.ResourceNotFoundException;
import com.cspot.insurahub.consumer.service.IdpIdMappingService;
import com.cspot.insurahub.denialreason.entity.DenialReason;
import com.cspot.insurahub.denialreason.repository.DenialReasonRepository;
import com.cspot.insurahub.enrollment.repository.EnrollmentRepository;
import com.cspot.insurahub.model.DenyClaimRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.cspot.insurahub.claim.testdata.ClaimTestData.createValidClaim;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimDenialServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private DenialReasonRepository denialReasonRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private PostgresReceiptStorage receiptStorage;

    @Mock
    private ClaimMapper claimMapper;

    @Mock
    private IdpIdMappingService idpIdMappingService;

    @InjectMocks
    private ClaimService claimService;

    @Test
    void shouldDenyClaimWithReason() {
        UUID claimId = UUID.randomUUID();
        UUID reasonId = UUID.randomUUID();
        Claim claim = createValidClaim();
        DenialReason reason = DenialReason.builder()
                .id(reasonId)
                .label("Duplicate claim")
                .description("This claim was already submitted.")
                .build();
        DenyClaimRequest request = new DenyClaimRequest().denialReasonId(reason.getId());

        when(claimRepository.findByIdOrThrow(claimId)).thenReturn(claim);
        when(denialReasonRepository.findById(reason.getId())).thenReturn(Optional.of(reason));

        claimService.denyClaim(claimId, request);

        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.DENIED);
        assertThat(claim.getDenialReason()).isSameAs(reason);
    }

    @Test
    void shouldRejectDenialWhenClaimIsNotPending() {
        UUID claimId = UUID.randomUUID();
        UUID reasonId = UUID.randomUUID();
        Claim claim = createValidClaim();
        claim.setStatus(ClaimStatus.APPROVED);

        when(claimRepository.findByIdOrThrow(claimId)).thenReturn(claim);

        assertThrows(
                DomainValidationException.class,
                () -> claimService.denyClaim(claimId, new DenyClaimRequest().denialReasonId(reasonId))
        );

        verifyNoInteractions(denialReasonRepository);
    }

    @Test
    void shouldRejectUnknownDenialReason() {
        UUID claimId = UUID.randomUUID();
        UUID reasonId = UUID.randomUUID();
        Claim claim = createValidClaim();
        when(claimRepository.findByIdOrThrow(claimId)).thenReturn(claim);
        when(denialReasonRepository.findById(reasonId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.denyClaim(claimId, new DenyClaimRequest().denialReasonId(reasonId))
        );

        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.PENDING);
    }
}
