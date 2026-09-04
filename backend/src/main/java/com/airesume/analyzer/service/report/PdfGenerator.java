package com.airesume.analyzer.service.report;

import com.airesume.analyzer.dto.CanonicalReportModel;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] generatePdfReportBytes(CanonicalReportModel model) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writePdfReport(baos, model);
        return baos.toByteArray();
    }

    public void generatePdfReportFile(String targetPath, CanonicalReportModel model) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(targetPath)) {
            writePdfReport(fos, model);
        }
    }

    private void writePdfReport(OutputStream os, CanonicalReportModel model) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter writer = PdfWriter.getInstance(document, os);

        // Footer Page Event Helper
        HeaderFooterPageEvent event = new HeaderFooterPageEvent();
        writer.setPageEvent(event);

        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(2, 132, 199));
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.DARK_GRAY);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(15, 23, 42));
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        Font mutedFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

        // 1. Executive Document Title Header
        Paragraph title = new Paragraph("EXECUTIVE ATS RESUME EVALUATION REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        document.add(title);

        String candidateInfoStr = String.format("Candidate: %s | File: %s",
                model != null ? model.getCandidateName() : "Candidate",
                model != null ? model.getOriginalFileName() : "resume.pdf");
        Paragraph sub = new Paragraph(sanitize(candidateInfoStr), subtitleFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(14);
        document.add(sub);

        // 2. Score Summary Table (PdfPTable with 4 columns)
        PdfPTable scoreTable = new PdfPTable(4);
        scoreTable.setWidthPercentage(100f);
        scoreTable.setWidths(new float[]{1f, 1f, 1f, 1f});
        scoreTable.setSpacingAfter(14f);

        String atsStr = model != null && model.getAtsScore() != null ? model.getAtsScore() + " / 100" : "Not analyzed";
        String grammarStr = model != null && model.getGrammarScore() != null ? model.getGrammarScore() + " / 100" : "Not analyzed";
        String matchStr = model != null && model.getJobMatchPercentage() != null ? model.getJobMatchPercentage() + "%" : "Not analyzed";
        String strengthStr = model != null && model.getOverallStrength() != null ? model.getOverallStrength() + " / 100" : "Not analyzed";

        addTableCell(scoreTable, "ATS Score", boldFont, new Color(241, 245, 249));
        addTableCell(scoreTable, atsStr, boldFont, Color.WHITE);
        addTableCell(scoreTable, "Grammar Index", boldFont, new Color(241, 245, 249));
        addTableCell(scoreTable, grammarStr, boldFont, Color.WHITE);

        addTableCell(scoreTable, "Target Match", boldFont, new Color(241, 245, 249));
        addTableCell(scoreTable, matchStr, boldFont, Color.WHITE);
        addTableCell(scoreTable, "Resume Strength", boldFont, new Color(241, 245, 249));
        addTableCell(scoreTable, strengthStr, boldFont, Color.WHITE);

        document.add(scoreTable);

        // 3. Section 1: Candidate Overview & Summary
        addSectionHeader(document, "1. Candidate Overview & Summary", sectionFont);
        if (model != null) {
            document.add(new Paragraph(sanitize("Name: " + model.getCandidateName() + " | Email: " + model.getCandidateEmail()), normalFont));
            document.add(new Paragraph(sanitize("Location: " + model.getCandidateLocation() + " | Phone: " + model.getCandidatePhone()), normalFont));
            Paragraph summaryPara = new Paragraph(sanitize("Summary: " + model.getSummary()), normalFont);
            summaryPara.setSpacingAfter(10f);
            document.add(summaryPara);
        }

        // 4. Section 2: ATS Keyword Analysis
        addSectionHeader(document, "2. ATS Keyword & Frequency Analysis", sectionFont);
        if (model != null) {
            document.add(new Paragraph(sanitize("Top Extracted Skills: " + joinList(model.getTopSkills())), normalFont));
            document.add(new Paragraph(sanitize("Missing Keywords: " + joinList(model.getMissingKeywords())), normalFont));
            Paragraph kwPara = new Paragraph(sanitize("Overused / Duplicate Words: " + joinList(model.getOverusedWords())), normalFont);
            kwPara.setSpacingAfter(10f);
            document.add(kwPara);
        }

        // 5. Section 3: Grammar & Writing Quality
        addSectionHeader(document, "3. Grammar, Readability & Action Verbs", sectionFont);
        if (model != null) {
            String weakStr = model.getGrammarWeakVerbCount() != null ? String.valueOf(model.getGrammarWeakVerbCount()) : "Not analyzed";
            String passiveStr = model.getGrammarPassiveCount() != null ? String.valueOf(model.getGrammarPassiveCount()) : "Not analyzed";
            document.add(new Paragraph(sanitize("Weak Action Verbs Count: " + weakStr), normalFont));
            document.add(new Paragraph(sanitize("Passive Voice Sentences Count: " + passiveStr), normalFont));
            Paragraph gramPara = new Paragraph(sanitize("Grammar Issues Detected: " + joinList(model.getGrammarIssues())), normalFont);
            gramPara.setSpacingAfter(10f);
            document.add(gramPara);
        }

        // 6. Section 4: Target Job Match Analysis
        addSectionHeader(document, "4. Target Job Match Analysis", sectionFont);
        if (model != null) {
            document.add(new Paragraph(sanitize("Target Job Title: " + model.getTargetJobTitle()), normalFont));
            document.add(new Paragraph(sanitize("Matched Job Skills: " + joinList(model.getMatchedJobSkills())), normalFont));
            Paragraph jobPara = new Paragraph(sanitize("Missing Job Skills: " + joinList(model.getMissingJobSkills())), normalFont);
            jobPara.setSpacingAfter(10f);
            document.add(jobPara);
        }

        // 7. Section 5: AI Recommendations & Evidence-Grounded Rewrites
        addSectionHeader(document, "5. AI Evidence-Grounded Recommendations", sectionFont);
        if (model != null) {
            document.add(new Paragraph(sanitize("Professional Summary Rewrite: " + model.getAiProfessionalSummary()), normalFont));
            document.add(new Paragraph(" ", normalFont));
            document.add(new Paragraph(sanitize("Quantified Experience Bullet Points: " + model.getAiImprovedExperience()), normalFont));
            document.add(new Paragraph(" ", normalFont));
            document.add(new Paragraph(sanitize("Project Stack Enhancements: " + model.getAiImprovedProjects()), normalFont));
        }

        document.close();
    }

    private void addSectionHeader(Document doc, String titleText, Font font) throws Exception {
        Paragraph p = new Paragraph(sanitize(titleText), font);
        p.setKeepTogether(true);
        p.setSpacingBefore(8f);
        p.setSpacingAfter(4f);
        doc.add(p);
    }

    private void addTableCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(sanitize(text), font));
        cell.setPadding(6f);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) return "Not analyzed / None";
        return String.join(", ", list);
    }

    /**
     * Sanitizes Unicode characters (smart quotes, em-dashes, bullets, symbols)
     * to safe Latin-1 / ASCII equivalents so OpenPDF core fonts never fail.
     */
    private String sanitize(String input) {
        if (input == null) return "";

        return input
                .replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("—", "-")
                .replace("–", "-")
                .replace("•", "- ")
                .replace("▪", "- ")
                .replace("►", "- ")
                .replace("✓", "[X]")
                .replace("★", "*")
                .replaceAll("[^\\x00-\\x7F\\xA0-\\xFF\\r\\n\\t]", "?");
    }

    /**
     * Page Event Helper for clean running footer ("Page X of Y").
     */
    private static class HeaderFooterPageEvent extends PdfPageEventHelper {
        private PdfTemplate totalPages;
        private BaseFont helveticaFont;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPages = writer.getDirectContent().createTemplate(30, 16);
            try {
                helveticaFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception ignored) {}
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();

            String footerText = String.format("ResuMatch ATS Evaluation System | Page %d of ", writer.getPageNumber());
            float textWidth = helveticaFont.getWidthPoint(footerText, 8);
            float x = (document.right() - document.left()) / 2 + document.leftMargin();
            float y = document.bottom() - 20;

            cb.beginText();
            cb.setFontAndSize(helveticaFont, 8);
            cb.setColorFill(Color.GRAY);
            cb.setTextMatrix(x - (textWidth / 2), y);
            cb.showText(footerText);
            cb.endText();

            cb.addTemplate(totalPages, x + (textWidth / 2), y);
            cb.restoreState();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPages.beginText();
            totalPages.setFontAndSize(helveticaFont, 8);
            totalPages.setColorFill(Color.GRAY);
            totalPages.showText(String.valueOf(writer.getPageNumber()));
            totalPages.endText();
        }
    }
}
