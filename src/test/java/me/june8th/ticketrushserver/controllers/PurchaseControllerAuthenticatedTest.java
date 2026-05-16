package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.services.PurchaseService;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerAuthenticatedTest {

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private ClientIPResolver clientIPResolver;

    private MockMvc mockMvc;
    private TestAuthenticatedAccount testAccount;

    @BeforeEach
    void setUp() {
        testAccount = TestAuthenticatedAccount.fromEnvironment();
        Assumptions.assumeTrue(testAccount.role() == me.june8th.ticketrushserver.types.Role.USER, "Authenticated user tests require TEST_AUTH_ACCOUNT_ROLE=USER");
        mockMvc = AuthenticatedRequestSupport.buildMockMvc(
                new PurchaseController(purchaseService),
                clientIPResolver
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPurchaseEvent_shouldReturnEventViewForAuthenticatedUser() throws Exception {
        when(purchaseService.getPurchaseEvent(testAccount.id(), 55L)).thenReturn(new PurchaseService.PurchaseEventView(
                55L,
                "Concert",
                Instant.parse("2026-06-15T12:00:00Z"),
                List.of(),
                List.of(),
                List.of(),
                null
        ));

        mockMvc.perform(get("/purchase/event/55")
                        .with(testAccount.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(55))
                .andExpect(jsonPath("$.eventName").value("Concert"));

        verify(purchaseService).getPurchaseEvent(testAccount.id(), 55L);
    }

    @Test
    void createHold_shouldPassAuthenticatedUserAndPayload() throws Exception {
        when(purchaseService.createHold(testAccount.id(), 55L, List.of(
                new PurchaseService.HoldItemRequest(101L, 201L),
                new PurchaseService.HoldItemRequest(102L, 202L)
        ))).thenReturn(new PurchaseService.HoldView(
                "hold-123",
                Instant.parse("2026-05-17T12:30:00Z"),
                5000L,
                List.of(
                        new PurchaseService.HeldItemView(101L, 201L, 2000L),
                        new PurchaseService.HeldItemView(102L, 202L, 3000L)
                )
        ));

        mockMvc.perform(post("/purchase/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(testAccount.requestPostProcessor())
                        .content("""
                                {
                                  "eventId": 55,
                                  "items": [
                                    {"seatId": 101, "ticketClassId": 201},
                                    {"seatId": 102, "ticketClassId": 202}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").value("hold-123"))
                .andExpect(jsonPath("$.totalAmount").value(5000))
                .andExpect(jsonPath("$.items[0].seatId").value(101));

        verify(purchaseService).createHold(testAccount.id(), 55L, List.of(
                new PurchaseService.HoldItemRequest(101L, 201L),
                new PurchaseService.HoldItemRequest(102L, 202L)
        ));
    }

    @Test
    void getHold_shouldReturnOwnedHold() throws Exception {
        when(purchaseService.getHold(testAccount.id(), "hold-123")).thenReturn(new PurchaseService.HoldView(
                "hold-123",
                Instant.parse("2026-05-17T12:30:00Z"),
                2500L,
                List.of(new PurchaseService.HeldItemView(101L, 201L, 2500L))
        ));

        mockMvc.perform(get("/purchase/hold/hold-123")
                        .with(testAccount.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").value("hold-123"))
                .andExpect(jsonPath("$.items[0].ticketClassId").value(201));

        verify(purchaseService).getHold(testAccount.id(), "hold-123");
    }

    @Test
    void releaseHold_shouldCallServiceAndReturnSuccess() throws Exception {
        doNothing().when(purchaseService).releaseHold(testAccount.id(), "hold-123");

        mockMvc.perform(delete("/purchase/hold/hold-123")
                        .with(testAccount.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Seat hold released successfully"));

        verify(purchaseService).releaseHold(testAccount.id(), "hold-123");
    }

    @Test
    void completeMockPayment_shouldReturnCompletedPurchase() throws Exception {
        when(purchaseService.completeMockPayment(testAccount.id(), "hold-123")).thenReturn(
                new PurchaseService.CompletedPurchaseView(900L, 5000L, List.of(1L, 2L))
        );

        mockMvc.perform(post("/purchase/mock-payment/hold-123")
                        .with(testAccount.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchaseId").value(900))
                .andExpect(jsonPath("$.amount").value(5000))
                .andExpect(jsonPath("$.ticketIds[1]").value(2));

        verify(purchaseService).completeMockPayment(testAccount.id(), "hold-123");
    }

}
