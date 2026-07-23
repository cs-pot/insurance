package com.cspot.insurahub.claim.validation;

import com.cspot.insurahub.claim.exception.InvalidReceiptException;
import com.cspot.insurahub.claim.storage.ReceiptStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptValidatorTest {

    @Mock
    private ReceiptStorageProperties receiptStorageProperties;

    private ReceiptValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ReceiptValidator(receiptStorageProperties);
    }

    @Test
    void shouldAcceptValidPdfReceipt() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofMegabytes(10));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.pdf",
                "application/pdf",
                new byte[1024]
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void shouldAcceptValidJpgReceipt() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofMegabytes(10));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.jpg",
                "image/jpeg",
                new byte[1024]
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void shouldAcceptValidPngReceipt() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofMegabytes(10));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.png",
                "image/png",
                new byte[1024]
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void shouldAcceptUpperCaseExtension() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofMegabytes(10));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.PDF",
                "application/pdf",
                new byte[1024]
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void shouldRejectNullReceipt() {
        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(null)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt file is required");
    }

    @Test
    void shouldRejectEmptyReceipt() {
        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.pdf",
                "application/pdf",
                new byte[0]
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt file is required");
    }

    @Test
    void shouldRejectReceiptAtMaxSize() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofBytes(100));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.pdf",
                "application/pdf",
                new byte[100]
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt file size must be less than 100B");
    }

    @Test
    void shouldRejectReceiptWithoutFileName() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofMegabytes(10));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                null,
                "application/pdf",
                new byte[100]
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt file name is required");
    }

    @Test
    void shouldRejectReceiptWithBlankFileName() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofMegabytes(10));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "   ",
                "application/pdf",
                new byte[100]
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt file name is required");
    }

    @Test
    void shouldRejectReceiptWithoutExtension() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofMegabytes(10));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt",
                "application/pdf",
                new byte[100]
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt must be a PDF, JPG, or PNG file");
    }

    @Test
    void shouldRejectReceiptWithUnsupportedExtension() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofMegabytes(10));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.gif",
                "image/gif",
                new byte[100]
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt must be a PDF, JPG, or PNG file");
    }

    @Test
    void shouldRejectReceiptWithUnsupportedContentType() {
        when(receiptStorageProperties.getMaxFileSize())
                .thenReturn(DataSize.ofMegabytes(10));

        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.pdf",
                "text/plain",
                new byte[100]
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt must be a PDF, JPG, or PNG file");
    }
}
