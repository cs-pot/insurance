package com.cspot.insurahub.claim.validation;

import com.cspot.insurahub.claim.exception.InvalidReceiptException;
import com.cspot.insurahub.claim.storage.ReceiptStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReceiptValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            "pdf",
            "jpg",
            "png"
    );

    private final ReceiptStorageProperties receiptStorageProperties;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidReceiptException("Receipt file is required");
        }

        long maxFileSizeBytes = receiptStorageProperties.getMaxFileSize().toBytes();

        if (file.getSize() >= maxFileSizeBytes) {
            throw new InvalidReceiptException(
                    "Receipt file size must be less than "
                            + receiptStorageProperties.getMaxFileSize()
            );
        }

        validateFileName(file.getOriginalFilename());

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidReceiptException(
                    "Receipt must be a PDF, JPG, or PNG file"
            );
        }
    }

    private void validateFileName(String fileName) {
        String sanitizedFileName = StringUtils.getFilename(fileName);

        if (!StringUtils.hasText(sanitizedFileName)) {
            throw new InvalidReceiptException("Receipt file name is required");
        }

        String extension = StringUtils.getFilenameExtension(sanitizedFileName);

        if (extension == null
                || !ALLOWED_FILE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new InvalidReceiptException(
                    "Receipt must be a PDF, JPG, or PNG file"
            );
        }
    }
}
