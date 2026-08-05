package com.cspot.insurahub.claim.validation;

import com.cspot.insurahub.claim.exception.InvalidReceiptException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

@Component
public class ReceiptValidator {

    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            "pdf",
            "jpg",
            "png"
    );

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidReceiptException("Receipt file is required");
        }

        String extension = validateFileName(file.getOriginalFilename());
        validateFileContent(file, extension);
    }

    private String validateFileName(String fileName) {
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

        return extension.toLowerCase(Locale.ROOT);
    }

    private void validateFileContent(MultipartFile file, String extension) {
        ReceiptFileType fileType = detectFileType(file);

        if (fileType == null || !fileType.extension.equals(extension)) {
            throw new InvalidReceiptException(
                    "Receipt must be a PDF, JPG, or PNG file"
            );
        }
    }

    private ReceiptFileType detectFileType(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(ReceiptFileType.LONGEST_SIGNATURE_LENGTH);

            return Arrays.stream(ReceiptFileType.values())
                    .filter(fileType -> fileType.matches(header))
                    .findFirst()
                    .orElse(null);
        } catch (IOException exception) {
            throw new InvalidReceiptException("Receipt file could not be read");
        }
    }

    private enum ReceiptFileType {
        PDF("pdf", new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D}),
        JPG("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
        PNG("png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

        private static final int LONGEST_SIGNATURE_LENGTH = Arrays.stream(values())
                .mapToInt(fileType -> fileType.signature.length)
                .max()
                .orElse(0);

        private final String extension;
        private final byte[] signature;

        ReceiptFileType(String extension, byte[] signature) {
            this.extension = extension;
            this.signature = signature;
        }

        private boolean matches(byte[] header) {
            if (header.length < signature.length) {
                return false;
            }

            for (int index = 0; index < signature.length; index++) {
                if (header[index] != signature[index]) {
                    return false;
                }
            }

            return true;
        }
    }
}
