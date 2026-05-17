package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.EventService;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.support.AuthenticatedRequestSupport;
import me.june8th.ticketrushserver.support.TestAuthenticatedAccount;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private EventService eventService;

    @Mock
    private StorageService storageService;

    @Mock
    private ClientIPResolver clientIPResolver;

    private MockMvc mockMvc;
    private TestAuthenticatedAccount account;

    @BeforeEach
    void setUp() {
        account = TestAuthenticatedAccount.builder()
                .id(77L)
                .name("Test Organization")
                .email("org@example.com")
                .role(Role.ORGANIZATION)
                .domain("77")
                .tokenVersion(0)
                .build();
        mockMvc = AuthenticatedRequestSupport.buildMockMvc(
                new OrganizationController(accountService, eventService, storageService),
                clientIPResolver
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getEvents_shouldReturnMinimumTicketPrice() throws Exception {
        Event event = createEvent(10L, "Organization Event");
        ArrayList<Event> events = new ArrayList<>(List.of(event));

        when(eventService.getAllOrganizationEvents(77L)).thenReturn(events);
        when(eventService.getMinimumTicketPrices(events)).thenReturn(Map.of(10L, 150000L));
        when(storageService.generatePresignedUrl("events/10.png")).thenReturn("https://cdn.example.com/events/10.png");

        mockMvc.perform(get("/organization/events").with(account.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Organization Event"))
                .andExpect(jsonPath("$[0].bannerUrl").value("https://cdn.example.com/events/10.png"))
                .andExpect(jsonPath("$[0].minimumTicketPrice").value(150000));

        verify(eventService).getAllOrganizationEvents(77L);
    }

    private Event createEvent(long id, String name) {
        return Event.builder()
                .id(id)
                .name(name)
                .organization(OrganizationAccount.builder()
                        .id(77L)
                        .name("Test Organization")
                        .email("org@example.com")
                        .passwordHash("hashed")
                        .build())
                .published(true)
                .isOnlineEvent(false)
                .venue("Venue " + id)
                .address("Address " + id)
                .dateTime(Instant.parse("2030-01-01T00:00:00Z"))
                .bannerKey("events/" + id + ".png")
                .build();
    }

}
