package com.cspot.insurahub.claim.validation;

import com.cspot.insurahub.claim.exception.InvalidReceiptException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReceiptValidatorTest {

    private ReceiptValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ReceiptValidator();
    }

    @Test
    void shouldAcceptValidPdfReceipt() {
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
    void shouldRejectReceiptWithoutFileName() {
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
