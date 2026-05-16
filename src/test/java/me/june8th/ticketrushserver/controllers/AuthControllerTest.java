package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.EmailService;
import me.june8th.ticketrushserver.support.TestCookies;
import me.june8th.ticketrushserver.types.Gender;
import me.june8th.ticketrushserver.utils.CookieUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final String OPERATION_ID = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456";

    private final MockMvc mockMvc;

    private final AccountService accountService;

    private final EmailService emailService;

    AuthControllerTest(@org.mockito.Mock AccountService accountService, @org.mockito.Mock EmailService emailService) {
        this.accountService = accountService;
        this.emailService = emailService;
        this.mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(accountService, emailService, 3600L)).build();
    }

    @Test
    void register_shouldReturnCreatedAndSetOperationCookie() throws Exception {
        when(accountService.userRegisterRequest(any(), any(), any(), any(), any())).thenReturn(OPERATION_ID);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice Doe",
                                  "email": "alice@example.com",
                                  "password": "Password1",
                                  "birthDate": 946684800000,
                                  "gender": "female"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(cookie().value(CookieUtils.OPERATION_ID_COOKIE_NAME, OPERATION_ID))
                .andExpect(cookie().httpOnly(CookieUtils.OPERATION_ID_COOKIE_NAME, true))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Waiting for confirmation"));
    }

    @Test
    void sendRegistrationOtp_shouldReturnSuccessForOperationCookie() throws Exception {
        doNothing().when(emailService).sendRegisterConfirmationEmail(OPERATION_ID);

        mockMvc.perform(get("/auth/register/confirmation")
                        .cookie(TestCookies.operationId(OPERATION_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Please check your email for the OTP code to confirm your registration"));

        verify(emailService).sendRegisterConfirmationEmail(OPERATION_ID);
    }

    @Test
    void confirmRegistration_shouldSetAccessTokenCookie() throws Exception {
        UserAccount account = UserAccount.builder()
                .id(42L)
                .name("Alice Doe")
                .email("alice@example.com")
                .passwordHash("hashed")
                .birthDate(new Date(946684800000L))
                .gender(Gender.FEMALE)
                .build();

        when(accountService.userRegisterConfirm(OPERATION_ID, "123456")).thenReturn(account);
        when(accountService.generateAccessToken(account)).thenReturn(ACCESS_TOKEN);

        mockMvc.perform(post("/auth/register/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(TestCookies.operationId(OPERATION_ID))
                        .content("""
                                {
                                  "otpCode": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().value(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, ACCESS_TOKEN))
                .andExpect(cookie().httpOnly(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, true))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.resourceId").value(42));
    }

    @Test
    void login_shouldSetAccessTokenCookie() throws Exception {
        UserAccount account = UserAccount.builder()
                .id(7L)
                .name("Alice Doe")
                .email("alice@example.com")
                .passwordHash("hashed")
                .birthDate(new Date(946684800000L))
                .gender(Gender.FEMALE)
                .build();

        when(accountService.accountLogin("alice@example.com", "Password1")).thenReturn(account);
        when(accountService.generateAccessToken(account)).thenReturn(ACCESS_TOKEN);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "alice@example.com",
                                  "password": "Password1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().value(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, ACCESS_TOKEN))
                .andExpect(cookie().httpOnly(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, true))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void logout_shouldClearAccessTokenCookie() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CookieUtils.ACCESS_TOKEN_COOKIE_NAME))
                .andExpect(cookie().maxAge(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, 0))
                .andExpect(cookie().httpOnly(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, true))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void resetPasswordRequest_shouldSetOperationCookie() throws Exception {
        when(accountService.accountResetPasswordRequest("alice@example.com", "Password2")).thenReturn(OPERATION_ID);

        mockMvc.perform(post("/auth/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "alice@example.com",
                                  "newPassword": "Password2"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(cookie().value(CookieUtils.OPERATION_ID_COOKIE_NAME, OPERATION_ID))
                .andExpect(cookie().httpOnly(CookieUtils.OPERATION_ID_COOKIE_NAME, true))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reset password token created"));
    }

    @Test
    void sendResetPasswordOtp_shouldReturnSuccessForOperationCookie() throws Exception {
        doNothing().when(emailService).sendPasswordResetEmail(OPERATION_ID);

        mockMvc.perform(get("/auth/reset/confirm")
                        .cookie(TestCookies.operationId(OPERATION_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Please check your email for the OTP code to confirm your password reset"));

        verify(emailService).sendPasswordResetEmail(OPERATION_ID);
    }

    @Test
    void confirmResetPassword_shouldReturnSuccess() throws Exception {
        doNothing().when(accountService).accountResetPasswordConfirm(OPERATION_ID, "123456");

        mockMvc.perform(post("/auth/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(TestCookies.operationId(OPERATION_ID))
                        .content("""
                                {
                                  "otpCode": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful"));

        verify(accountService).accountResetPasswordConfirm(eq(OPERATION_ID), eq("123456"));
    }

}
