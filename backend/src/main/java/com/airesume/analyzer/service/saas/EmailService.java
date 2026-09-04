package com.airesume.analyzer.service.saas;

public interface EmailService {

    void sendWelcomeEmail(String toEmail, String fullName);

    void sendResumeUploadNotification(String toEmail, String fileName);

    void sendAnalysisCompleteNotification(String toEmail, String fileName, int atsScore);

    void sendReportGeneratedNotification(String toEmail, String reportName);
}
