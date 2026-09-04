package com.airesume.analyzer.security;

import com.airesume.analyzer.exception.InvalidFileTypeException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileSecurityValidatorTest {

    private FileSecurityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileSecurityValidator();
    }

    private byte[] createValidPdfBytes() throws Exception {
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createValidDocxBytes() throws Exception {
        try (XWPFDocument docx = new XWPFDocument()) {
            docx.createParagraph().createRun().setText("Sample Resume Content");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            docx.write(baos);
            return baos.toByteArray();
        }
    }

    @Test
    @DisplayName("Should successfully validate a genuine PDF file with magic bytes and valid structure")
    void testValidPdfValidation() throws Exception {
        byte[] pdfBytes = createValidPdfBytes();
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        assertDoesNotThrow(() -> validator.validateFile(file));
    }

    @Test
    @DisplayName("Should successfully validate a genuine DOCX file with PK container and POI structure")
    void testValidDocxValidation() throws Exception {
        byte[] docxBytes = createValidDocxBytes();
        MockMultipartFile file = new MockMultipartFile("file", "resume.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes);

        assertDoesNotThrow(() -> validator.validateFile(file));
    }

    @Test
    @DisplayName("Should reject file when extension is spoofed but magic bytes are invalid (e.g. EXE disguised as PDF)")
    void testMagicBytesSpoofingRejection() {
        byte[] fakeBytes = new byte[]{0x4D, 0x5A, (byte) 0x90, 0x00}; // MZ Executable header
        MockMultipartFile file = new MockMultipartFile("file", "malicious.pdf", "application/pdf", fakeBytes);

        assertThrows(InvalidFileTypeException.class, () -> validator.validateFile(file));
    }

    @Test
    @DisplayName("Should reject filenames attempting path traversal with '..' or path separators")
    void testFilenamePathTraversalRejection() throws Exception {
        byte[] pdfBytes = createValidPdfBytes();
        MockMultipartFile file = new MockMultipartFile("file", "../../../etc/passwd.pdf", "application/pdf", pdfBytes);

        assertThrows(InvalidFileTypeException.class, () -> validator.validateFile(file));
    }

    @Test
    @DisplayName("Should reject unsupported file extensions (e.g. .exe, .sh, .bat)")
    void testUnsupportedExtensionRejection() {
        MockMultipartFile file = new MockMultipartFile("file", "script.sh", "text/x-shellscript", "echo hello".getBytes());

        assertThrows(InvalidFileTypeException.class, () -> validator.validateFile(file));
    }

    @Test
    @DisplayName("Should throw SecurityException when resolveSafePath detects path escape attempt")
    void testResolveSafePathTraversalDetection() {
        Path baseDir = Paths.get("uploads/resumes").toAbsolutePath().normalize();

        assertThrows(SecurityException.class, () -> validator.resolveSafePath(baseDir, "../../secret/file.txt"));
    }
}
