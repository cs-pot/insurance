package com.cspot.insurahub.claim.service;

import com.cspot.insurahub.claim.entity.Claim;
import com.cspot.insurahub.claim.entity.Receipt;
import com.cspot.insurahub.claim.repository.ClaimRepository;
import com.cspot.insurahub.claim.repository.ReceiptRepository;
import com.cspot.insurahub.claim.storage.PostgresReceiptStorage;
import com.cspot.insurahub.common.exception.ResourceNotFoundException;
import com.cspot.insurahub.enrollment.entity.Enrollment;
import com.cspot.insurahub.enrollment.repository.EnrollmentRepository;
import com.cspot.insurahub.model.PostClaimRequest;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ReceiptRepository receiptRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PostgresReceiptStorage receiptStorage;

    @Transactional
    public PostResponse createClaim(PostClaimRequest request, MultipartFile receipt) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found with id: " + request.getEnrollmentId()
                ));

        Claim claim = new Claim(
                enrollment,
                request.getServiceDate(),
                request.getAmount()
        );

        log.debug(
                "Creating Claim: enrollmentId={}, serviceDate={}, amount={}",
                enrollment.getId(),
                request.getServiceDate(),
                request.getAmount()
        );

        Claim savedClaim = claimRepository.save(claim);
        log.info("Claim created: id={}", savedClaim.getId());

        Receipt savedReceipt = receiptStorage.store(savedClaim, receipt);
        log.info("Receipt created: id={}", savedReceipt.getId());

        return new PostResponse(savedClaim.getId());
    }

    @Transactional(readOnly = true)
    public Resource getReceipt(UUID claimId) {
        Receipt receipt = receiptRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Receipt not found with claim id: " + claimId));

        return new InputStreamResource(new ByteArrayInputStream(receipt.getContent()));
    }

    @Transactional
    public void denyClaim(UUID claimId) {
        Claim claim = claimRepository.findByIdOrThrow(claimId);
        claim.deny();
        log.info("Claim denied: id={}", claimId);
    }
}
