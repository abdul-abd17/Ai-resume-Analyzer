package com.airesume.analyzer.service.saas;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    @Async("saasTaskExecutor")
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String subject = "Welcome to ResuMatch ATS Evaluation Platform!";
        String body = "<div style='font-family:sans-serif;padding:20px;'><h2 style='color:#0284c7;'>Welcome, " + fullName + "!</h2><p>Your account is ready. Upload your resume to calculate your ATS score and unlock AI recommendations.</p></div>";
        sendHtmlMail(toEmail, subject, body);
    }

    @Override
    @Async("saasTaskExecutor")
    public void sendResumeUploadNotification(String toEmail, String fileName) {
        String subject = "Resume Upload Confirmed: " + fileName;
        String body = "<div style='font-family:sans-serif;padding:20px;'><h3 style='color:#0284c7;'>Upload Success</h3><p>Your resume file <strong>" + fileName + "</strong> has been uploaded and queued for parsing.</p></div>";
        sendHtmlMail(toEmail, subject, body);
    }

    @Override
    @Async("saasTaskExecutor")
    public void sendAnalysisCompleteNotification(String toEmail, String fileName, int atsScore) {
        String subject = "ATS Evaluation Complete - Score: " + atsScore + "/100";
        String body = "<div style='font-family:sans-serif;padding:20px;'><h3 style='color:#10b981;'>Analysis Complete</h3><p>Your resume <strong>" + fileName + "</strong> scored <strong>" + atsScore + "/100</strong>.</p></div>";
        sendHtmlMail(toEmail, subject, body);
    }

    @Override
    @Async("saasTaskExecutor")
    public void sendReportGeneratedNotification(String toEmail, String reportName) {
        String subject = "Your Evaluation Report is Ready: " + reportName;
        String body = "<div style='font-family:sans-serif;padding:20px;'><h3 style='color:#8b5cf6;'>Report Ready</h3><p>Your PDF evaluation report <strong>" + reportName + "</strong> is ready for download.</p></div>";
        sendHtmlMail(toEmail, subject, body);
    }

    private void sendHtmlMail(String toEmail, String subject, String bodyHtml) {
        if (mailSender == null) {
            log.info("[EMAIL SERVICE FALLBACK LOG] To: {}, Subject: {}, Body: {}", toEmail, subject, bodyHtml);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@resumatch-ats.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(bodyHtml, true);
            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}. Logged email body instead.", toEmail, e.getMessage());
        }
    }
}
