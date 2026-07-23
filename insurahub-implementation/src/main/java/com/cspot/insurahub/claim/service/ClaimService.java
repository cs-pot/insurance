package com.cspot.insurahub.claim.service;

import com.cspot.insurahub.claim.entity.Claim;
import com.cspot.insurahub.claim.entity.Receipt;
import com.cspot.insurahub.claim.repository.ClaimRepository;
import com.cspot.insurahub.claim.repository.ReceiptRepository;
import com.cspot.insurahub.claim.storage.PostgresReceiptStorage;
import com.cspot.insurahub.common.exception.ResourceNotFoundException;
import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.consumer.repository.ConsumerRepository;
import com.cspot.insurahub.model.PostClaimRequest;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.repository.InsurancePlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ReceiptRepository receiptRepository;
    private final ConsumerRepository consumerRepository;
    private final InsurancePlanRepository insurancePlanRepository;
    private final PostgresReceiptStorage receiptStorage;

    @Transactional
    public PostResponse createClaim(PostClaimRequest request, MultipartFile receipt) {
        Consumer employee = consumerRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consumer not found with id: " + request.getEmployeeId()
                ));

        InsurancePlan plan = insurancePlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insurance plan not found with id: " + request.getPlanId()
                ));

        Claim claim = new Claim(
                employee,
                plan,
                request.getServiceDate(),
                request.getAmount()
        );

        log.debug(
                "Creating Claim: name={}, payroll={}, startDate={}, endDate={}",
                employee,
                plan,
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
    public Resource getReceipt(UUID receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Receipt not found with id: " + receiptId));

        return new ByteArrayResource(receipt.getContent());
    }
}
