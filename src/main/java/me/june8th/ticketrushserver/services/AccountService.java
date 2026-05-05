package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.*;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.repositories.RegisterRequestRepository;
import me.june8th.ticketrushserver.repositories.ResetPasswordRequestRepository;
import me.june8th.ticketrushserver.repositories.UserRepository;
import me.june8th.ticketrushserver.security.AccessTokenData;
import me.june8th.ticketrushserver.security.AccessTokenProvider;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.Validator;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenProvider accessTokenProvider;
    private final RegisterRequestRepository registerRequestRepository;
    private final ResetPasswordRequestRepository resetPasswordRequestRepository;

    /**
     * Create a new user registration request. This will create a new entry in the RegisterRequest table.
     *
     * @param name full name of the user
     * @param email email address (must be unique)
     * @param password plain text password (will be hashed before saving)
     * @param birthDate birth date
     * @param country country of residence
     * @return key of the created RegisterRequest, which can be used to confirm the registration
     */
    @NullMarked
    @Transactional
    public String userRegisterRequest(String name, String email, String password, Date birthDate, Gender gender, Country country) {
        Validator.create()
                .validateName(name)
                .validateEmail(email)
                .validatePassword(password)
                .validateBirthDate(birthDate)
                .throwExceptionIfInvalid();

        if (registerRequestRepository.findByEmail(email).isPresent()) {
            throw new RatelimitedException("A registration session with this email already exists");
        }

        if (accountRepository.existsByEmail(email)) {
            throw new ResourceConflictException("This email is already registered");
        }

        String passwordHash = passwordEncoder.encode(password);
        assert passwordHash != null;

        RegisterRequest registerRequest = RegisterRequest.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordHash)
                .birthDate(birthDate)
                .genderString(gender.toString())
                .country(country)
                .build();

        logger.debug("Creating registration request {} for email {}", registerRequest.getKey(), email);
        registerRequestRepository.save(registerRequest);
        return registerRequest.getKey();
    }

    /**
     * Confirm a user registration request by providing the OTP code sent to the user's email address.
     * If the OTP code is correct and the registration request is still valid, a new UserAccount will be created.
     *
     * @param key     the key of the RegisterRequest entry to confirm
     * @param otpCode the OTP code sent to the user's email address
     */
    @NullMarked
    @Transactional
    public void userRegisterConfirm(String key, String otpCode) {
        Validator.create()
                .validateRequestKey(key)
                .validateOtpCode(otpCode)
                .throwExceptionIfInvalid();

        RegisterRequest registerRequest = registerRequestRepository.findByKey(key).orElseThrow(
                () -> new ResourceNotFoundException("Invalid request")
        );

        Gender gender = Gender.fromString(registerRequest.getGenderString());

        if (Instant.now().isAfter(registerRequest.getExpiresAt())) {
            throw new TimedOutException("This registration request has expired");
        }

        if (registerRequest.getAvailableAttempts() <= 0) {
            throw new RatelimitedException("No available attempts left for this registration request");
        }

        if (!registerRequest.getOtpCode().equals(otpCode)) {
            registerRequest.setAvailableAttempts(registerRequest.getAvailableAttempts() - 1);
            registerRequestRepository.save(registerRequest);
            throw new AuthenticationFailedException("Invalid OTP code");
        }

        if (accountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new InvalidStateException("This email is already registered");
        }

        UserAccount userAccount = UserAccount.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .passwordHash(registerRequest.getPasswordHash())
                .birthDate(registerRequest.getBirthDate())
                .gender(gender)
                .country(registerRequest.getCountry())
                .build();

        logger.debug("Validation passed, creating userAccount account for {} ({})", registerRequest.getName(), registerRequest.getEmail());
        try {
            registerRequestRepository.delete(registerRequest);
        } catch (Exception e) {
            logger.warn("Failed to delete registration request with key {}: {}", key, e.getMessage());
        }
        userRepository.save(userAccount);
    }

    /**
     * Authenticate a user by their email and password.
     * If the credentials are correct, return the corresponding UserAccount.
     *
     * @param email the user's email address
     * @param password the user's plain text password
     * @return the authenticated UserAccount
     */
    @NullMarked
    public Account accountLogin(String email, String password) {
        Validator.create()
                .validateEmail(email)
                .validatePassword(password)
                .throwExceptionIfInvalid();

        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new AuthenticationFailedException("Invalid credentials")
        );

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid credentials");
        }

        logger.debug("Account {} ({}) logged in successfully", account.getId(), account.getName());
        return account;
    }


    /**
     * Generate a new access token for the specified account.
     *
     * @param account account
     * @return token string
     */
    @NullMarked
    public String generateAccessToken(Account account) {
        return accessTokenProvider.generateAccessToken(AccessTokenData.builder()
                .id(account.getId())
                .type(account.getType())
                .domain(account.getDomain())
                .version(account.getTokenVersion())
                .build());
    }

    /**
     * Invalidate all active sessions for the specified account by incrementing the token version.
     *
     * @param accountId the ID of the account whose sessions should be invalidated
     * @return true if the operation is successful, false otherwise
     */
    @NullMarked
    @Transactional
    public boolean accountLogoutAllSessions(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        account.setTokenVersion(account.getTokenVersion() + 1);
        accountRepository.save(account);
        return true;
    }

    /**
     * @param email the email address of the user requesting a password reset
     * @param newPassword the new plain text password for that user (will be hashed before saving)
     * @return the key of the created ResetPasswordRequest, which can be used to confirm the password reset
     */
    @NullMarked
    @Transactional
    public String accountResetPasswordRequest(String email, String newPassword) {
        Validator.create()
                .validateEmail(email)
                .validatePassword(newPassword)
                .throwExceptionIfInvalid();

        if (resetPasswordRequestRepository.findByEmail(email).isPresent()) {
            throw new RatelimitedException("A password reset session with this account already exists");
        }

        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("This email is not registered")
        );

        String newPasswordHash = passwordEncoder.encode(newPassword);
        assert newPasswordHash != null;

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .accountId(account.getId())
                .email(email)
                .oldPasswordHash(account.getPasswordHash())
                .newPasswordHash(newPasswordHash)
                .build();

        logger.debug("Creating password reset request {} for email {}", resetPasswordRequest.getKey(), email);
        resetPasswordRequestRepository.save(resetPasswordRequest);
        return resetPasswordRequest.getKey();
    }

    /**
     * Confirm a user password reset request by providing the OTP code sent to the user's email address.
     * If the OTP code is correct and the password reset request is still valid, the user's password will be updated.
     *
     * @param token the key of the ResetPasswordRequest entry to confirm
     * @param otpCode the OTP code sent to the user's email address
     */
    @NullMarked
    @Transactional
    public void accountResetPasswordConfirm(String token, String otpCode) {
        Validator.create()
                .validateRequestKey(token)
                .validateOtpCode(otpCode)
                .throwExceptionIfInvalid();

        ResetPasswordRequest resetPasswordRequest = resetPasswordRequestRepository.findByKey(token).orElseThrow(
                () -> new ResourceNotFoundException("Invalid request")
        );

        if (Instant.now().isAfter(resetPasswordRequest.getExpiresAt())) {
            throw new TimedOutException("This password reset request has expired");
        }

        if (resetPasswordRequest.getAvailableAttempts() <= 0) {
            throw new RatelimitedException("No available attempts left for this password reset request");
        }

        if (!resetPasswordRequest.getOtpCode().equals(otpCode)) {
            resetPasswordRequest.setAvailableAttempts(resetPasswordRequest.getAvailableAttempts() - 1);
            resetPasswordRequestRepository.save(resetPasswordRequest);
            throw new AuthenticationFailedException("Invalid OTP code");
        }

        Account account = accountRepository.findById(resetPasswordRequest.getAccountId()).orElseThrow(
                () -> new InvalidStateException("Invalid request")
        );
        if (!account.getEmail().equals(resetPasswordRequest.getEmail())) {
            throw new InvalidStateException("Invalid request");
        }
        if (!account.getPasswordHash().equals(resetPasswordRequest.getOldPasswordHash())) {
            throw new InvalidStateException("Invalid request");
        }

        account.setPasswordHash(resetPasswordRequest.getNewPasswordHash());
        accountRepository.save(account);

        logger.debug("Password reset successful for account {} ({})", account.getId(), account.getEmail());
        try {
            resetPasswordRequestRepository.delete(resetPasswordRequest);
        } catch (Exception e) {
            logger.warn("Failed to delete password reset request with key {}: {}", token, e.getMessage());
        }
    }

    @NullMarked
    @Transactional
    public void changeAccountEmail(Long accountId, String newEmail, String currentPassword) {
        Validator.create()
                .validateEmail(newEmail)
                .throwExceptionIfInvalid();

        if (accountRepository.existsByEmail(newEmail)) {
            throw new ResourceConflictException("This email is already registered");
        }

        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        account.setEmail(newEmail);
        accountRepository.save(account);
    }

}
