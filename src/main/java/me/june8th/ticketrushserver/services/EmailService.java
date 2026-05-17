package me.june8th.ticketrushserver.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.Purchase;
import me.june8th.ticketrushserver.data.Ticket;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.temp.RegisterRequest;
import me.june8th.ticketrushserver.temp.ResetPasswordRequest;
import me.june8th.ticketrushserver.temp.RegisterRequestRepository;
import me.june8th.ticketrushserver.temp.ResetPasswordRequestRepository;
import me.june8th.ticketrushserver.utils.Validator;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final long RESEND_COOLDOWN = 90L;
    private static final String REGISTER_CONFIRMATION_SUBJECT = "Confirm your TicketRush registration";
    private static final String PASSWORD_RESET_SUBJECT = "Reset your TicketRush password";
    private static final String TICKET_INFORMATION_SUBJECT = "Your TicketRush tickets";
    private static final DateTimeFormatter EMAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final RegisterRequestRepository registerRequestRepository;
    private final ResetPasswordRequestRepository resetPasswordRequestRepository;

    @Value("${app.mail}")
    private String mailFromAddress;

    /**
     * Send an email containing an OTP code that the user can use to confirm their registration.
     *
     * @param key the key of the RegisterPayload entry
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

        log.debug("Sending registration confirmation email to {} with OTP code {}", toAddress, otpCode);
        sendHtmlEmail(toAddress, REGISTER_CONFIRMATION_SUBJECT, emailContent);
    }

    /**
     * Send an email containing an OTP code that the user can use to reset their password.
     *
     * @param key the key of the ResetPasswordPayload entry
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

        log.debug("Sending password reset email to {} with OTP code {}", toAddress, otpCode);
        sendHtmlEmail(toAddress, PASSWORD_RESET_SUBJECT, emailContent);
    }

    @NullMarked
    public void sendTicketInformationEmail(UserAccount user, Purchase purchase, List<Ticket> tickets) throws MessagingException {
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("Ticket information email requires at least one ticket");
        }

        String toAddress = user.getEmail();
        String userName = user.getName();
        Validator.create()
                .validateEmail(toAddress)
                .validateName(userName)
                .throwExceptionIfInvalid();

        Event event = tickets.getFirst().getTicketClass().getSalesRound().getEvent();

        Context ctx = new Context();
        ctx.setVariable("userName", userName);
        ctx.setVariable("purchaseId", purchase.getId());
        ctx.setVariable("purchaseAmount", formatAmount(purchase.getAmount()));
        ctx.setVariable("purchaseAt", formatInstant(purchase.getAt()));
        ctx.setVariable("eventName", event.getName());
        ctx.setVariable("eventDateTime", formatInstant(event.getDateTime()));
        ctx.setVariable("venue", event.getVenue());
        ctx.setVariable("address", event.getAddress());
        ctx.setVariable("tickets", tickets.stream().map(this::toTicketEmailItem).toList());
        String emailContent = templateEngine.process("ticket_information_email", ctx);

        log.debug("Sending ticket information email to {} for purchase {}", toAddress, purchase.getId());
        sendHtmlEmail(toAddress, TICKET_INFORMATION_SUBJECT, emailContent);
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

    private TicketEmailItem toTicketEmailItem(Ticket ticket) {
        return new TicketEmailItem(
                ticket.getId(),
                ticket.getTicketClass().getName(),
                ticket.getSeat().getSeatRow().getSeatZone().getName(),
                ticket.getSeat().getSeatRow().getLabel(),
                ticket.getSeat().getNumber(),
                lastSecretCodeCharacters(ticket.getTicketSecretCode())
        );
    }

    private String lastSecretCodeCharacters(String secretCode) {
        if (secretCode == null || secretCode.length() <= 8) {
            return secretCode;
        }
        return secretCode.substring(secretCode.length() - 8);
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "N/A";
        }
        return EMAIL_DATE_FORMAT.format(instant);
    }

    private String formatAmount(Long amount) {
        if (amount == null) {
            return "N/A";
        }
        return String.format(Locale.US, "%,d VND", amount);
    }

    private record TicketEmailItem(long id, String ticketClassName, String seatZoneName, String seatRowLabel, int seatNumber, String secretCode) {
    }

}
