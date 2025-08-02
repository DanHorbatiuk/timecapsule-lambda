package dev.horbatiuk.timecapsule.service;

import dev.horbatiuk.timecapsule.exception.AppException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private static final String VERIFICATION_EMAIL_SUBJECT = "Email Verification";
    private static final String CAPSULE_LIMIT_REACHED_SUBJECT = "Capsule Limit Reached";
    private static final String COPYRIGHT_NOTICE = "© 2025 TimeCapsule. All rights reserved.";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Async
    public void sendVerificationEmail(String to, String verificationUrl) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("verificationUrl", verificationUrl);
        variables.put("copyrightNotice", COPYRIGHT_NOTICE);
        String htmlContent = buildEmailContent(variables);
        sendEmail(to, htmlContent);
    }

    private void sendEmail(String to, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(EmailSenderService.VERIFICATION_EMAIL_SUBJECT);
            helper.setText(htmlContent, true);
        } catch (MessagingException e) {
            throw new AppException("Email sending failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        mailSender.send(message);
    }

    private String buildEmailContent(Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process("email/verification/verification-email", context);
    }

}
