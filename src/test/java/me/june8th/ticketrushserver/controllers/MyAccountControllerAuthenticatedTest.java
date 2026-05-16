package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.support.AuthenticatedRequestSupport;
import me.june8th.ticketrushserver.support.TestAuthenticatedAccount;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MyAccountControllerAuthenticatedTest {

    @Mock
    private AccountService accountService;

    @Mock
    private StorageService storageService;

    @Mock
    private ClientIPResolver clientIPResolver;

    private MockMvc mockMvc;
    private TestAuthenticatedAccount testAccount;

    @BeforeEach
    void setUp() {
        testAccount = TestAuthenticatedAccount.fromEnvironment();
        Assumptions.assumeTrue(testAccount.role() == me.june8th.ticketrushserver.types.Role.USER, "Authenticated user tests require TEST_AUTH_ACCOUNT_ROLE=USER");
        mockMvc = AuthenticatedRequestSupport.buildMockMvc(
                new MyAccountController(accountService, storageService),
                clientIPResolver
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAccount_shouldReturnAuthenticatedUserData() throws Exception {
        when(accountService.getAccountData(testAccount.id())).thenReturn(testAccount.toAccount());
        when(storageService.generatePresignedUrl(testAccount.avatarKey())).thenReturn(testAccount.avatarKey() == null ? null : "https://cdn.example.com/avatar");

        mockMvc.perform(get("/account")
                        .with(testAccount.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccount.id()))
                .andExpect(jsonPath("$.name").value(testAccount.name()))
                .andExpect(jsonPath("$.email").value(testAccount.email()));

        verify(accountService).getAccountData(testAccount.id());
    }

    @Test
    void changeName_shouldReturnSuccessForValidPayload() throws Exception {
        doNothing().when(accountService).changeAccountName(testAccount.id(), "Updated Name");

        mockMvc.perform(put("/account/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(testAccount.requestPostProcessor())
                        .content("""
                                {
                                  "newName": "Updated Name"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Name changed successfully"));

        verify(accountService).changeAccountName(testAccount.id(), "Updated Name");
    }

    @Test
    void logoutAll_shouldClearCookieAndCallService() throws Exception {
        doNothing().when(accountService).accountLogoutAllSessions(testAccount.id());

        mockMvc.perform(post("/account/logout-all")
                        .with(testAccount.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out from all devices. Please log in again."));

        verify(accountService).accountLogoutAllSessions(testAccount.id());
    }

}
