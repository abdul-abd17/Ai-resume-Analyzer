package com.airesume.analyzer.security;

import com.airesume.analyzer.exception.InvalidFileTypeException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class FileSecurityValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_UNCOMPRESSED_ZIP_SIZE = 30 * 1024 * 1024; // 30 MB Zip Bomb protection
    private static final int MAX_ZIP_ENTRY_COUNT = 1000;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/x-pdf"
    );

    // Magic Bytes signatures
    private static final byte[] PDF_MAGIC_BYTES = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] ZIP_MAGIC_BYTES = new byte[]{0x50, 0x4B, 0x03, 0x04}; // PK.. (DOCX container)

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("File is empty. Please upload a valid PDF or DOCX resume.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileTypeException("File size exceeds maximum allowable limit of 10 MB.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileTypeException("Filename cannot be empty.");
        }

        // 1. Filename & Path Traversal Validation
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\") || originalFilename.contains("\0")) {
            throw new InvalidFileTypeException("Security Error: Invalid filename path traversal detected.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileTypeException("Unsupported file extension: ." + extension + ". Only PDF and DOCX files are allowed.");
        }

        // 2. MIME Type Validation
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileTypeException("Unsupported MIME type: " + contentType);
        }

        // 3. Magic Bytes Check & Content Verification
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new InvalidFileTypeException("Failed to read file content: " + e.getMessage());
        }

        if ("pdf".equals(extension)) {
            validatePdf(bytes);
        } else if ("docx".equals(extension)) {
            validateDocx(bytes);
        }
    }

    private void validatePdf(byte[] bytes) {
        if (bytes.length < 4 || !matchBytes(bytes, PDF_MAGIC_BYTES)) {
            throw new InvalidFileTypeException("Security Error: File header magic bytes do not match valid PDF format.");
        }

        // Document Readability Check
        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.isEncrypted()) {
                throw new InvalidFileTypeException("Encrypted PDF files are not supported.");
            }
        } catch (Exception e) {
            throw new InvalidFileTypeException("Corrupted PDF document: Unable to parse PDF structure.");
        }
    }

    private void validateDocx(byte[] bytes) {
        if (bytes.length < 4 || !matchBytes(bytes, ZIP_MAGIC_BYTES)) {
            throw new InvalidFileTypeException("Security Error: File header magic bytes do not match valid DOCX container format.");
        }

        // Zip Bomb & Zip Slip Defense
        long totalUncompressedBytes = 0;
        int entryCount = 0;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_ZIP_ENTRY_COUNT) {
                    throw new InvalidFileTypeException("Security Error: Too many entries inside DOCX zip container (Zip bomb threshold exceeded).");
                }

                String entryName = entry.getName();
                if (entryName.contains("..") || entryName.startsWith("/") || entryName.startsWith("\\")) {
                    throw new InvalidFileTypeException("Security Error: Zip Slip path traversal attempt detected inside DOCX container: " + entryName);
                }

                long entrySize = entry.getSize();
                if (entrySize > 0) {
                    totalUncompressedBytes += entrySize;
                }

                if (totalUncompressedBytes > MAX_UNCOMPRESSED_ZIP_SIZE) {
                    throw new InvalidFileTypeException("Security Error: Decompression limit exceeded (possible ZIP bomb attack).");
                }
            }
        } catch (InvalidFileTypeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidFileTypeException("Malformed DOCX container structure.");
        }

        // POI Document Readability Check
        try (InputStream is = new ByteArrayInputStream(bytes);
             XWPFDocument docx = new XWPFDocument(is)) {
            if (docx.getParagraphs() == null) {
                throw new InvalidFileTypeException("Corrupted DOCX document.");
            }
        } catch (Exception e) {
            throw new InvalidFileTypeException("Corrupted DOCX document: Unable to parse Word document structure.");
        }
    }

    private boolean matchBytes(byte[] src, byte[] magic) {
        for (int i = 0; i < magic.length; i++) {
            if (src[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot + 1);
    }

    /**
     * Prevents Path Traversal when resolving storage paths.
     */
    public Path resolveSafePath(Path baseDir, String fileName) {
        Path targetPath = baseDir.resolve(fileName).normalize();
        if (!targetPath.startsWith(baseDir.normalize())) {
            throw new SecurityException("Security Exception: Path traversal attempt detected for file " + fileName);
        }
        return targetPath;
    }
}
