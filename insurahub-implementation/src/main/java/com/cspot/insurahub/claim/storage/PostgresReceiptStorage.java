package com.cspot.insurahub.claim.storage;

import com.cspot.insurahub.claim.entity.Claim;
import com.cspot.insurahub.claim.entity.Receipt;
import com.cspot.insurahub.claim.exception.ReceiptStorageException;
import com.cspot.insurahub.claim.repository.ReceiptRepository;
import com.cspot.insurahub.claim.validation.ReceiptValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PostgresReceiptStorage {

    private final ReceiptRepository receiptRepository;
    private final ReceiptValidator receiptValidator;

    public Receipt store(Claim claim, MultipartFile file) {
        validateClaim(claim);
        receiptValidator.validate(file);

        try {
            Receipt receipt = new Receipt(
                    claim,
                    StringUtils.getFilename(file.getOriginalFilename()),
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes()
            );

            return receiptRepository.save(receipt);
        } catch (IOException exception) {
            throw new ReceiptStorageException(
                    "Failed to read receipt file",
                    exception
            );
        }
    }

    private void validateClaim(Claim claim) {
        Objects.requireNonNull(claim, "claim must not be null");

        if (claim.getId() == null) {
            throw new IllegalArgumentException(
                    "claim must be persisted before storing receipt"
            );
        }
    }
}
