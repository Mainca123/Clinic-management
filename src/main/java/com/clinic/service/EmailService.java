package com.clinic.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@ApplicationScoped
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    @ConfigProperty(name = "app.verify.base-url")
    String verifyBaseUrl;

    @ConfigProperty(name = "clinic.system.name", defaultValue = "Clinic System")
    String systemName;

    @ConfigProperty(name = "clinic.system.address", defaultValue = "Hanoi, Vietnam")
    String systemAddress;

    @ConfigProperty(name = "sendgrid.api.key")
    String sendGridApiKey;

    @ConfigProperty(name = "sendgrid.from.email")
    String sendGridFromEmail;

    @ConfigProperty(name = "resend.api.key")
    String resendApiKey;


    //Gửi email xác thực tài khoản khi đăng ký
    public void sendVerificationEmail(String toEmail, String token) {
        String verifyLink = verifyBaseUrl + token;
        String subject = "[" + systemName + "] Xác thực tài khoản";
        System.out.println("API1" + sendGridApiKey);
        System.out.println("API2" + resendApiKey);

        executeEmailWithFallback(toEmail, subject, "verification-email.html", verifyLink, null);
    }

    //Gửi email thông báo mật khẩu mới khi người dùng quên mật khẩu
    public void sendResetPasswordEmail(String toEmail, String newPassword) {
        String subject = "[" + systemName + "] Mật khẩu mới của bạn";

        executeEmailWithFallback(toEmail, subject, "reset-password.html", null, newPassword);
    }

    private void executeEmailWithFallback(String toEmail,
                                          String subject,
                                          String templateName,
                                          String verifyLink,
                                          String newPassword) {
        try {
            sendWithResend(toEmail, subject, templateName, verifyLink, newPassword);
            LOGGER.info("Email sent successfully via Resend to: " + toEmail);
        } catch (Exception e) {
            LOGGER.warning("Resend failed: " + e.getMessage() + ". Switching to SendGrid...");
            try {
                sendWithSendGrid(toEmail, subject, templateName, verifyLink, newPassword);
                LOGGER.info("Email sent successfully via SendGrid to: " + toEmail);
            } catch (Exception ex) {
                LOGGER.severe("Both Resend and SendGrid failed! Error: " + ex.getMessage());
                throw new RuntimeException("All email services are down.");
            }
        }
    }

    private void sendWithResend(String toEmail, String subject, String templateName, String verifyLink, String newPassword) throws Exception {
        String html = new String(loadAndFillTemplate(templateName, verifyLink, newPassword)
                .getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        Resend resend = new Resend(resendApiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(systemName + " <onboarding@resend.dev>")
                .to(toEmail)
                .subject(subject)
                .html(html)
                .build();

        resend.emails().send(params);
    }

    private void sendWithSendGrid(String toEmail, String subject, String templateName, String verifyLink, String newPassword) throws Exception {
        String html = loadAndFillTemplate(templateName, verifyLink, newPassword);

        Email from = new Email(sendGridFromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", html);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("SendGrid API error: " + response.getBody());
        }
    }

    private String loadAndFillTemplate(String fileName, String verifyLink, String newPassword) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("templates/" + fileName)) {

            if (is == null) {
                throw new RuntimeException("Template not found: " + fileName);
            }

            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            content = content
                    .replace("{{SYSTEM_NAME}}", systemName)
                    .replace("{{SYSTEM_ADDRESS}}", systemAddress);

            if (verifyLink != null) {
                content = content.replace("{{VERIFY_LINK}}", verifyLink);
            }
            if (newPassword != null) {
                content = content.replace("{{NEW_PASSWORD}}", newPassword);
            }

            return content;

        } catch (Exception e) {
            throw new RuntimeException("Error processing email template", e);
        }
    }
}