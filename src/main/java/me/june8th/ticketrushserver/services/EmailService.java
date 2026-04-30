package me.june8th.ticketrushserver.services;

import me.june8th.ticketrushserver.utils.Validator;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

        String emailContent = templateEngine.process("register_confirmation_email", ctx);
        sendText(toAddress, REGISTER_CONFIRMATION_SUBJECT, emailContent);
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

        String emailContent = templateEngine.process("password_reset_email", ctx);
        sendText(toAddress, PASSWORD_RESET_SUBJECT, emailContent);
    }

    @NullMarked
    private void sendText(String toAddress, String subject, String otpCode) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(otpCode);
        mailSender.send(message);
    }

}

