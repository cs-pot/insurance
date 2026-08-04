package com.cspot.insurahub.claim.validation;

import com.cspot.insurahub.claim.exception.InvalidReceiptException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

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
                pdfContent()
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void shouldAcceptValidJpgReceipt() {
        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.jpg",
                "image/jpeg",
                jpgContent()
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void shouldAcceptValidPngReceipt() {
        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.png",
                "image/png",
                pngContent()
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void shouldAcceptUpperCaseExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.PDF",
                "application/pdf",
                pdfContent()
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
                pdfContent()
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
                pdfContent()
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
                pdfContent()
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
                pdfContent()
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt must be a PDF, JPG, or PNG file");
    }

    @Test
    void shouldAcceptReceiptWhenReportedContentTypeIsUnreliable() {
        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.pdf",
                "text/plain",
                pdfContent()
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void shouldRejectReceiptWithUnsupportedContent() {
        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.pdf",
                "application/pdf",
                "not a pdf".getBytes(StandardCharsets.UTF_8)
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt must be a PDF, JPG, or PNG file");
    }

    @Test
    void shouldRejectReceiptWhenContentDoesNotMatchExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "receipt",
                "receipt.pdf",
                "application/pdf",
                pngContent()
        );

        InvalidReceiptException exception = assertThrows(
                InvalidReceiptException.class,
                () -> validator.validate(file)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt must be a PDF, JPG, or PNG file");
    }

    private byte[] pdfContent() {
        return "%PDF-1.7\n".getBytes(StandardCharsets.UTF_8);
    }

    private byte[] jpgContent() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
    }

    private byte[] pngContent() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }
}
