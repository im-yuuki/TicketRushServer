package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.PurchaseService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerAuthenticatedTest {

    @Mock
    private AccountService accountService;

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private StorageService storageService;

    @Mock
    private ClientIPResolver clientIPResolver;

    private MockMvc mockMvc;
    private TestAuthenticatedAccount testAccount;

    @BeforeEach
    void setUp() {
        testAccount = TestAuthenticatedAccount.fromApplicationTestConfig();
        Assumptions.assumeTrue(testAccount.role() == me.june8th.ticketrushserver.types.Role.USER, "Authenticated user tests require test.auth.account.role=USER in application-test.yml");
        mockMvc = AuthenticatedRequestSupport.buildMockMvc(
                new UserController(accountService, purchaseService, storageService),
                clientIPResolver
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPurchasedTickets_shouldReturnAuthenticatedUserTickets() throws Exception {
        when(purchaseService.getUserTickets(testAccount.id())).thenReturn(List.of(
                new PurchaseService.UserTicketView(
                        10L,
                        Instant.parse("2026-05-17T10:15:30Z"),
                        20L,
                        30L,
                        "Sample Event",
                        Instant.parse("2026-06-01T19:00:00Z"),
                        "General Sale",
                        "VIP",
                        "Front Zone",
                        "A",
                        12,
                        "SECRET-123",
                        null
                )
        ));

        mockMvc.perform(get("/user/tickets")
                        .with(testAccount.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketId").value(10))
                .andExpect(jsonPath("$[0].eventName").value("Sample Event"))
                .andExpect(jsonPath("$[0].ticketClassName").value("VIP"));

        verify(purchaseService).getUserTickets(testAccount.id());
    }

    @Test
    void updateAvatar_shouldCallServiceForAuthenticatedUser() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[] {1, 2, 3, 4});
        doNothing().when(accountService).updateAvatar(testAccount.id(), file);

        mockMvc.perform(multipart("/user/avatar")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(testAccount.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Avatar updated successfully"));

        verify(accountService).updateAvatar(testAccount.id(), file);
    }

}
