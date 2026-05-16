package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.services.PurchaseService;
import me.june8th.ticketrushserver.support.AuthenticatedRequestSupport;
import me.june8th.ticketrushserver.support.TestAuthenticatedAccount;
import me.june8th.ticketrushserver.types.PurchaseData;
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
        testAccount = TestAuthenticatedAccount.fromApplicationTestConfig();
        Assumptions.assumeTrue(testAccount.role() == me.june8th.ticketrushserver.types.Role.USER, "Authenticated user tests require test.auth.account.role=USER in application-test.yml");
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
        when(purchaseService.getPurchaseEvent(testAccount.id(), 55L)).thenReturn(new PurchaseData.PurchaseEventView(
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
    void getSeatStatuses_shouldReturnSeatStatusesForAuthenticatedUser() throws Exception {
        when(purchaseService.getSeatStatuses(testAccount.id(), 55L)).thenReturn(new PurchaseData.SeatStatusCollectionView(
                55L,
                List.of(
                        new PurchaseData.SeatZoneView(
                                1L,
                                "Front Zone",
                                100,
                                200,
                                List.of(
                                        new PurchaseData.SeatRowView(
                                                10L,
                                                0,
                                                "A",
                                                List.of(
                                                        new PurchaseData.SeatView(101L, 0, 1, PurchaseData.SeatAvailability.AVAILABLE),
                                                        new PurchaseData.SeatView(102L, 1, 2, PurchaseData.SeatAvailability.HELD)
                                                )
                                        )
                                )
                        )
                ),
                null
        ));

        mockMvc.perform(get("/purchase/event/55/seats/status")
                        .with(testAccount.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(55))
                .andExpect(jsonPath("$.seatZones[0].rows[0].seats[1].availability").value("HELD"));

        verify(purchaseService).getSeatStatuses(testAccount.id(), 55L);
    }

    @Test
    void createHold_shouldPassAuthenticatedUserAndEventId() throws Exception {
        when(purchaseService.createHold(testAccount.id(), 55L)).thenReturn(new PurchaseData.HoldView(
                "hold-123",
                Instant.parse("2026-05-17T12:30:00Z"),
                0L,
                List.of()
        ));

        mockMvc.perform(post("/purchase/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(testAccount.requestPostProcessor())
                        .content("""
                                {
                                  "eventId": 55
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").value("hold-123"))
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());

        verify(purchaseService).createHold(testAccount.id(), 55L);
    }

    @Test
    void addSeatToHold_shouldPassAuthenticatedUserAndSeatPayload() throws Exception {
        when(purchaseService.addSeatToHold(testAccount.id(), "hold-123", new PurchaseData.HoldItemRequest(101L, 201L)))
                .thenReturn(new PurchaseData.HoldView(
                        "hold-123",
                        Instant.parse("2026-05-17T12:30:00Z"),
                        2000L,
                        List.of(new PurchaseData.HeldItemView(101L, 201L, 2000L))
                ));

        mockMvc.perform(post("/purchase/hold/hold-123/seat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(testAccount.requestPostProcessor())
                        .content("""
                                {
                                  "seatId": 101,
                                  "ticketClassId": 201
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").value("hold-123"))
                .andExpect(jsonPath("$.totalAmount").value(2000))
                .andExpect(jsonPath("$.items[0].seatId").value(101));

        verify(purchaseService).addSeatToHold(testAccount.id(), "hold-123", new PurchaseData.HoldItemRequest(101L, 201L));
    }

    @Test
    void getHold_shouldReturnOwnedHold() throws Exception {
        when(purchaseService.getHold(testAccount.id(), "hold-123")).thenReturn(new PurchaseData.HoldView(
                "hold-123",
                Instant.parse("2026-05-17T12:30:00Z"),
                2500L,
                List.of(new PurchaseData.HeldItemView(101L, 201L, 2500L))
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
    void completeMockPayment_shouldReturnCompletedPurchase() {
        when(purchaseService.completeMockPayment(testAccount.id(), "hold-123")).thenReturn(
                new PurchaseData.CompletedPurchaseView(900L, 5000L, List.of(1L, 2L))
        );

        var response = new PurchaseController(purchaseService).completeMockPayment(testAccount.id(), "hold-123");

        org.junit.jupiter.api.Assertions.assertEquals(200, response.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertNotNull(response.getBody());
        org.junit.jupiter.api.Assertions.assertEquals(900L, response.getBody().purchaseId());
        org.junit.jupiter.api.Assertions.assertEquals(5000L, response.getBody().amount());
        org.junit.jupiter.api.Assertions.assertEquals(List.of(1L, 2L), response.getBody().ticketIds());

        verify(purchaseService).completeMockPayment(testAccount.id(), "hold-123");
    }

}
