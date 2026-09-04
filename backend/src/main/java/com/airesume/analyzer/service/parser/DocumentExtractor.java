package com.airesume.analyzer.service.parser;

import com.airesume.analyzer.exception.FileStorageException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class DocumentExtractor {

    public String extractRawText(File file, String originalFileName) {
        String fileNameLower = originalFileName.toLowerCase();

        if (fileNameLower.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else if (fileNameLower.endsWith(".docx") || fileNameLower.endsWith(".doc")) {
            return extractTextFromDocx(file);
        } else if (fileNameLower.endsWith(".txt")) {
            return extractTextFromTxt(file);
        } else {
            throw new FileStorageException("Unsupported file extension for parsing: " + originalFileName);
        }
    }

    private String extractTextFromTxt(File file) {
        try {
            return java.nio.file.Files.readString(file.toPath());
        } catch (IOException e) {
            throw new FileStorageException("Failed to read text document: " + e.getMessage(), e);
        }
    }

    private String extractTextFromPdf(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            throw new FileStorageException("Failed to extract text from PDF document: " + e.getMessage(), e);
        }
    }

    private String extractTextFromDocx(File file) {
        try (InputStream is = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        } catch (Exception e) {
            throw new FileStorageException("Failed to extract text from Word document: " + e.getMessage(), e);
        }
    }
}
