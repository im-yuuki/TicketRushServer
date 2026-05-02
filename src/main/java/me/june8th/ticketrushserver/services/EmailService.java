package me.june8th.ticketrushserver.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.RegisterRequest;
import me.june8th.ticketrushserver.data.ResetPasswordRequest;
import me.june8th.ticketrushserver.repositories.RegisterRequestRepository;
import me.june8th.ticketrushserver.repositories.ResetPasswordRequestRepository;
import me.june8th.ticketrushserver.utils.Validator;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private static final long RESEND_COOLDOWN = 90L;
    private static final String REGISTER_CONFIRMATION_SUBJECT = "Confirm your TicketRush registration";
    private static final String PASSWORD_RESET_SUBJECT = "Reset your TicketRush password";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final RegisterRequestRepository registerRequestRepository;
    private final ResetPasswordRequestRepository resetPasswordRequestRepository;

    @Value("${app.mail}")
    private String mailFromAddress;

    /**
     * Send an email containing an OTP code that the user can use to confirm their registration.
     *
     * @param key the key of the RegisterRequest entry
     * @throws MessagingException if there is an error while sending the email
     */
    @NullMarked
    public void sendRegisterConfirmationEmail(String key) throws MessagingException {
        RegisterRequest registerRequest = registerRequestRepository.findByKey(key).orElseThrow(
                () -> new IllegalArgumentException("Invalid registration key")
        );

        if (registerRequest.getAvailableAttempts() <= 0) {
            throw new RuntimeException("No available attempts left for this registration request");
        }

        Instant now = Instant.now();
        if (now.isAfter(registerRequest.getExpiresAt())) {
            throw new RuntimeException("This registration request has expired");
        }
        if (now.isBefore(registerRequest.getNextResendAvailable())) {
            throw new RuntimeException("You are in cooldown period. Please wait before requesting another email.");
        } else {
            registerRequest.setNextResendAvailable(Instant.now().plusSeconds(RESEND_COOLDOWN));
            registerRequestRepository.save(registerRequest);
        }

        String toAddress = registerRequest.getEmail();
        String userName = registerRequest.getName();
        String otpCode = registerRequest.getOtpCode();

        Validator.create()
                .validateEmail(toAddress)
                .validateName(userName)
                .validateNotBlank(otpCode)
                .throwExceptionIfInvalid();

        Context ctx = new Context();
        ctx.setVariable("userName", userName);
        ctx.setVariable("otpCode", otpCode);
        String emailContent = templateEngine.process("register_confirmation_email", ctx);

        logger.debug("Sending registration confirmation email to {} with OTP code {}", toAddress, otpCode);
        sendHtmlEmail(toAddress, REGISTER_CONFIRMATION_SUBJECT, emailContent);
    }

    /**
     * Send an email containing an OTP code that the user can use to reset their password.
     *
     * @param key the key of the ResetPasswordRequest entry
     * @throws MessagingException if there is an error while sending the email
     */
    @NullMarked
    public void sendPasswordResetEmail(String key) throws MessagingException {
        ResetPasswordRequest resetPasswordRequest = resetPasswordRequestRepository.findByKey(key).orElseThrow(
                () -> new IllegalArgumentException("Invalid key: " + key)
        );

        if (resetPasswordRequest.getAvailableAttempts() <= 0) {
            throw new RuntimeException("No available attempts left for this password reset request");
        }

        Instant now = Instant.now();
        if (now.isAfter(resetPasswordRequest.getExpiresAt())) {
            throw new RuntimeException("This password reset request has expired");
        }
        if (now.isBefore(resetPasswordRequest.getNextResendAvailable())) {
            throw new RuntimeException("You are in cooldown period. Please wait before requesting another email.");
        } else {
            resetPasswordRequest.setNextResendAvailable(Instant.now().plusSeconds(RESEND_COOLDOWN));
            resetPasswordRequestRepository.save(resetPasswordRequest);
        }

        String toAddress = resetPasswordRequest.getEmail();
        String otpCode = resetPasswordRequest.getOtpCode();
        Validator.create()
                .validateEmail(toAddress)
                .validateNotBlank(otpCode)
                .throwExceptionIfInvalid();

        Context ctx = new Context();
        ctx.setVariable("userName", toAddress);
        ctx.setVariable("otpCode", otpCode);
        String emailContent = templateEngine.process("password_reset_email", ctx);

        logger.debug("Sending password reset email to {} with OTP code {}", toAddress, otpCode);
        sendHtmlEmail(toAddress, PASSWORD_RESET_SUBJECT, emailContent);
    }

    @NullMarked
    private void sendHtmlEmail(String toAddress, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailFromAddress);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

}

