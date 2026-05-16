package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.june8th.ticketrushserver.data.*;
import me.june8th.ticketrushserver.temp.RegisterRequest;
import me.june8th.ticketrushserver.temp.ResetPasswordRequest;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.temp.RegisterRequestRepository;
import me.june8th.ticketrushserver.temp.ResetPasswordRequestRepository;
import me.june8th.ticketrushserver.repositories.UserRepository;
import me.june8th.ticketrushserver.repositories.OrganizationAccountRepository;
import me.june8th.ticketrushserver.repositories.FollowRepository;
import me.june8th.ticketrushserver.types.AccessTokenData;
import me.june8th.ticketrushserver.security.AccessTokenProvider;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.RandomGenerator;
import me.june8th.ticketrushserver.utils.Validator;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenProvider accessTokenProvider;
    private final RegisterRequestRepository registerRequestRepository;
    private final ResetPasswordRequestRepository resetPasswordRequestRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final FollowRepository followRepository;
    private final StorageService storageService;

    /**
     * Create a new user registration request. This will create a new entry in the RegisterPayload table.
     *
     * @param name      full name of the user
     * @param email     email address (must be unique)
     * @param password  plain text password (will be hashed before saving)
     * @param birthDate birth date
     * @param gender    gender
     * @return key of the created RegisterPayload, which can be used to confirm the registration
     */
    @NullMarked
    public String userRegisterRequest(String name, String email, String password, Date birthDate, Gender gender) {
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
                .build();

        registerRequestRepository.save(registerRequest);
        log.debug("Successfully created registration request with key: {} for email: {}", registerRequest.getKey(), email);
        return registerRequest.getKey();
    }

    /**
     * Confirm a user registration request by providing the OTP code sent to the user's email address.
     * If the OTP code is correct and the registration request is still valid, a new UserAccount will be created.
     *
     * @param key     the key of the RegisterPayload entry to confirm
     * @param otpCode the OTP code sent to the user's email address
     */
    @NullMarked
    public Account userRegisterConfirm(String key, String otpCode) {
        Validator.create()
                .validateRequestKey(key)
                .validateOtpCode(otpCode)
                .throwExceptionIfInvalid();

        RegisterRequest registerRequest = registerRequestRepository.findByKey(key).orElseThrow(
                () -> {
                    log.warn("Registration request with key {} not found.", key);
                    return new ResourceNotFoundException("Invalid request");
                }
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
                .build();

        try {
            registerRequestRepository.delete(registerRequest);
        } catch (Exception e) {
            log.warn("Failed to delete registration request with key {}: {}", key, e.getMessage());
        }
        Account savedAccount = userRepository.save(userAccount);
        log.debug("Successfully confirmed registration and created user account with ID: {} for email: {}", savedAccount.getId(), savedAccount.getEmail());
        return savedAccount;
    }

    /**
     * Authenticate a user by their email and password.
     * If the credentials are correct, return the corresponding UserAccount.
     *
     * @param email    the user's email address
     * @param password the user's plain text password
     * @return the authenticated UserAccount
     */
    @NullMarked
    public Account accountLogin(String email, String password) {
        Validator.create()
                .validateEmail(email)
                .throwExceptionIfInvalid();

        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new AuthenticationFailedException("Invalid credentials")
        );

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid credentials");
        }

        log.debug("Account {} ({}) logged in successfully", account.getId(), account.getName());
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
                .role(account.getRole())
                .domain(account.getDomain())
                .version(account.getTokenVersion())
                .build());
    }

    /**
     * Invalidate all active sessions for the specified account by incrementing the token version.
     *
     * @param accountId the ID of the account whose sessions should be invalidated
     */
    @NullMarked
    public void accountLogoutAllSessions(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        account.setTokenVersion(account.getTokenVersion() + 1);
        accountRepository.save(account);
        log.debug("Successfully invalidated all sessions for account ID: {}", accountId);
    }

    /**
     * @param email       the email address of the user requesting a password reset
     * @param newPassword the new plain text password for that user (will be hashed before saving)
     * @return the key of the created ResetPasswordPayload, which can be used to confirm the password reset
     */
    @NullMarked
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

        resetPasswordRequestRepository.save(resetPasswordRequest);
        log.debug("Successfully created password reset request with key: {} for email: {}", resetPasswordRequest.getKey(), email);
        return resetPasswordRequest.getKey();
    }

    /**
     * Confirm a user password reset request by providing the OTP code sent to the user's email address.
     * If the OTP code is correct and the password reset request is still valid, the user's password will be updated.
     *
     * @param token   the key of the ResetPasswordPayload entry to confirm
     * @param otpCode the OTP code sent to the user's email address
     */
    @NullMarked
    public void accountResetPasswordConfirm(String token, String otpCode) {
        Validator.create()
                .validateRequestKey(token)
                .validateOtpCode(otpCode)
                .throwExceptionIfInvalid();

        ResetPasswordRequest resetPasswordRequest = resetPasswordRequestRepository.findByKey(token).orElseThrow(
                () -> {
                    log.warn("Password reset request with token {} not found.", token);
                    return new ResourceNotFoundException("Invalid request");
                }
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

        log.debug("Password reset successful for account {} ({})", account.getId(), account.getEmail());
        try {
            resetPasswordRequestRepository.delete(resetPasswordRequest);
        } catch (Exception e) {
            log.warn("Failed to delete password reset request with key {}: {}", token, e.getMessage());
        }
    }

    @NullMarked
    public Account getAccountData(Long id) {
        return accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
    }

    @NullMarked
    public UserAccount getUserData(long id) {
        return getUserAccount(id);
    }

    /**
     * Change the name of an account.
     *
     * @param accountId account ID
     * @param newName   new full name
     */
    @NullMarked
    public void changeAccountName(Long accountId, String newName) {
        Validator.create()
                .validateName(newName)
                .throwExceptionIfInvalid();
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        account.setName(newName);
        accountRepository.save(account);
        log.debug("Successfully changed name for account ID: {} to {}", accountId, newName);
    }

    /**
     * Change the email of an account.
     *
     * @param accountId       account ID
     * @param newEmail        new email address
     * @param currentPassword current password of the account
     */
    @NullMarked
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
        if (passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            String oldEmail = account.getEmail();
            account.setEmail(newEmail);
            accountRepository.save(account);
            log.debug("Successfully changed email for account ID: {} from {} to {}", accountId, oldEmail, newEmail);
            return;
        }
        log.debug("Incorrect password provided for account ID: {} during email change.", accountId);
        throw new AuthenticationFailedException("Incorrect password");
    }

    /**
     * Change the password of an account.
     *
     * @param accountId       account ID
     * @param currentPassword current password
     * @param newPassword     new password
     */
    @NullMarked
    public void changeAccountPassword(Long accountId, String currentPassword, String newPassword) {
        Validator.create()
                .validatePassword(newPassword)
                .throwExceptionIfInvalid();
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            account.setPasswordHash(passwordEncoder.encode(newPassword));
            accountRepository.save(account);
            log.debug("Successfully changed password for account ID: {}", accountId);
            return;
        }
        log.debug("Incorrect password provided for account ID: {} during password change.", accountId);
        throw new AuthenticationFailedException("Incorrect password");
    }

    @NullMarked
    public void lockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (account.isLocked()) {
            log.debug("Account {} ({}) is already locked", accountId, account.getName());
            throw new InvalidStateException("Account is already locked");
        }
        account.setLocked(true);
        accountRepository.save(account);
        log.debug("Successfully locked account {} ({})", accountId, account.getName());
    }

    @NullMarked
    public void unlockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (!account.isLocked()) {
            log.debug("Account {} ({}) is already unlocked", accountId, account.getName());
            throw new InvalidStateException("Account is already unlocked");
        }
        account.setLocked(false);
        accountRepository.save(account);
        log.debug("Successfully unlocked account {} ({})", accountId, account.getName());
    }

    @NullMarked
    public OrganizationAccount createOrganizationAccount(String name, String email, String password) {
        Validator.create()
                .validateName(name)
                .validateEmail(email)
                .validatePassword(password)
                .throwExceptionIfInvalid();
        if (accountRepository.existsByEmail(email)) {
            throw new ResourceConflictException("This email is already registered");
        }
        OrganizationAccount organization = OrganizationAccount.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .verified(false)
                .build();
        OrganizationAccount savedOrganization = organizationAccountRepository.save(organization);
        log.debug("Successfully created organization account {} ({})", savedOrganization.getId(), savedOrganization.getEmail());
        return savedOrganization;
    }

    @NullMarked
    public void verifyOrganizationAccount(Long organizationId) {
        OrganizationAccount organization = organizationAccountRepository.findById(organizationId).orElseThrow(
                () -> new ResourceNotFoundException("Organization account not found")
        );
        if (organization.getVerified()) {
            throw new InvalidStateException("Organization is already verified");
        }
        organization.setVerified(true);
        organizationAccountRepository.save(organization);
        log.debug("Successfully verified organization account {} ({})", organization.getId(), organization.getEmail());
    }

    @NullMarked
    public void updateOrganizationInfo(long id, UpdateOrganizationInfoPayload payload) {
        OrganizationAccount organization = organizationAccountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organization account not found")
        );
        String originalAliasName = organization.getAliasName();
        payload.patchOrganization(organization);
        if (organization.getAliasName() != null && !organization.getAliasName().isEmpty()) {
            if (!organization.getAliasName().equals(originalAliasName) && organizationAccountRepository.existsByAliasName(organization.getAliasName())) {
                throw new ResourceConflictException("This alias name is already taken");
            }
        }
        OrganizationAccount updatedOrganization = organizationAccountRepository.save(organization);
        log.debug("Successfully updated information for organization account {} ({})", updatedOrganization.getId(), updatedOrganization.getEmail());
    }

    @NullMarked
    public void updateUserInfo(long id, UpdateUserInfoPayload payload) {
        UserAccount user = getUserAccount(id);
        payload.patchUser(user);
        UserAccount updatedUser = userRepository.save(user);
        log.debug("Successfully updated information for user account {} ({})", updatedUser.getId(), updatedUser.getEmail());
    }

    @NullMarked
    public void updateAvatar(long id, MultipartFile avatar) throws IOException {
        Validator.create()
                .validateImageFile(avatar, 10 * 1024 * 1024)
                .throwExceptionIfInvalid();
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        String avatarKey = generateImageContentKey("avatars", account.getId(), avatar.getContentType());
        switch (account) {
            case UserAccount user -> {
                storageService.upload(avatar.getBytes(), avatarKey, avatar.getContentType());
                user.setAvatarKey(avatarKey);
                userRepository.save(user);
                log.debug("Successfully updated avatar for user account {} ({})", user.getId(), user.getEmail());
            }
            case OrganizationAccount organization -> {
                storageService.upload(avatar.getBytes(), avatarKey, avatar.getContentType());
                organization.setAvatarKey(avatarKey);
                organizationAccountRepository.save(organization);
                log.debug("Successfully updated avatar for organization account {} ({})", organization.getId(), organization.getEmail());
            }
            default -> throw new InvalidStateException("This account type does not support avatar updates");
        }
    }

    @NullMarked
    public void updateOrganizationBanner(long id, MultipartFile banner) throws IOException {
        Validator.create()
                .validateImageFile(banner, 20 * 1024 * 1024)
                .throwExceptionIfInvalid();
        OrganizationAccount organization = organizationAccountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organization account not found")
        );
        String bannerKey = generateImageContentKey("banners", organization.getId(), banner.getContentType());
        storageService.upload(banner.getBytes(), bannerKey, banner.getContentType());
        organization.setBannerKey(bannerKey);
        organizationAccountRepository.save(organization);
        log.debug("Successfully updated banner for organization account {} ({})", organization.getId(), organization.getEmail());
    }

    @NullMarked
    public OrganizationAccount getPublicOrganizationById(long id) {
        return organizationAccountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organization not found")
        );
    }

    @NullMarked
    public OrganizationAccount getPublicOrganizationByAlias(String alias) {
        return organizationAccountRepository.findByAliasName(alias).orElseThrow(
                () -> new ResourceNotFoundException("Organization not found")
        );
    }

    @NullMarked
    public long getOrganizationFollowerCount(long orgId) {
        OrganizationAccount organization = getPublicOrganizationById(orgId);
        return followRepository.countByOrganization(organization);
    }

    @NullMarked
    public void followOrganization(long userId, long orgId) {
        UserAccount user = getUserAccount(userId);
        OrganizationAccount organization = getPublicOrganizationById(orgId);
        if (followRepository.existsByFollowerAndOrganization(user, organization)) {
            throw new InvalidStateException("Already following this organization");
        }
        Follow follow = Follow.builder()
                .follower(user)
                .organization(organization)
                .build();
        followRepository.save(follow);
        log.debug("User {} followed organization {}", userId, orgId);
    }

    @NullMarked
    public void unfollowOrganization(long userId, long orgId) {
        UserAccount user = getUserAccount(userId);
        OrganizationAccount organization = getPublicOrganizationById(orgId);
        Follow follow = followRepository.findByFollowerAndOrganization(user, organization).orElseThrow(
                () -> new InvalidStateException("You are not following this organization")
        );
        followRepository.delete(follow);
        log.debug("User {} unfollowed organization {}", userId, orgId);
    }

    private UserAccount getUserAccount(long userId) {
        Account account = accountRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (account instanceof UserAccount user) {
            return user;
        }
        throw new ForbiddenException("Only user accounts can follow organizations");
    }

    private String generateImageContentKey(String prefix, long accountId, String contentType) {
        return prefix + "/" + accountId + "/" + RandomGenerator.generateRandomString(16) + switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case null -> throw new InvalidStateException("Content-Type is missing");
            default -> throw new InvalidStateException("Unsupported image type");
        };
    }

}
