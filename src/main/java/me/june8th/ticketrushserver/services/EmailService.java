package me.june8th.ticketrushserver.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import me.june8th.ticketrushserver.utils.Validator;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final String REGISTER_CONFIRMATION_SUBJECT = "Confirm your TicketRush registration";
    private static final String PASSWORD_RESET_SUBJECT = "Reset your TicketRush password";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromAddress = fromAddress;
    }

    @NullMarked
    public void sendRegisterConfirmationEmail(String toAddress, String userName, String otpCode) throws MailException {
        new Validator()
                .validateEmail(toAddress)
                .validateName(userName)
                .validateNotBlank(otpCode)
                .throwExceptionIfInvalid();

        Context ctx = new Context();
        ctx.setVariable("userName", userName);
        ctx.setVariable("otpCode", otpCode);

        String emailContent = templateEngine.process("register-confirmation-email", ctx);
        sendHtml(toAddress, REGISTER_CONFIRMATION_SUBJECT, emailContent);
    }

    @NullMarked
    public void sendPasswordResetEmail(String toAddress, String userName, String otpCode) throws MailException {
        new Validator()
                .validateEmail(toAddress)
                .validateName(userName)
                .validateNotBlank(otpCode)
                .throwExceptionIfInvalid();

        Context ctx = new Context();
        ctx.setVariable("userName", userName);
        ctx.setVariable("otpCode", otpCode);

        String emailContent = templateEngine.process("password-reset-email", ctx);
        sendHtml(toAddress, PASSWORD_RESET_SUBJECT, emailContent);
    }

    @NullMarked
    private void sendHtml(String toAddress, String subject, String emailContent) throws MailException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailPreparationException("Failed to prepare email message", e);
        }
    }

}
