package me.june8th.ticketrushserver.services;

import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.data.Seat;
import me.june8th.ticketrushserver.data.Ticket;
import me.june8th.ticketrushserver.data.TicketClass;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.repositories.EventRepository;
import me.june8th.ticketrushserver.repositories.OrganizationAccountRepository;
import me.june8th.ticketrushserver.repositories.SalesRoundRepository;
import me.june8th.ticketrushserver.repositories.SeatRepository;
import me.june8th.ticketrushserver.repositories.SeatRowRepository;
import me.june8th.ticketrushserver.repositories.SeatZoneRepository;
import me.june8th.ticketrushserver.repositories.TicketClassRepository;
import me.june8th.ticketrushserver.types.InvalidStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OrganizationAccountRepository organizationAccountRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SalesRoundRepository salesRoundRepository;

    @Mock
    private SeatZoneRepository seatZoneRepository;

    @Mock
    private SeatRowRepository seatRowRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketClassRepository ticketClassRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StorageService storageService;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private EventService eventService;

    @Test
    void publishEvent_shouldRejectWhenEventHasNoTicketClasses() {
        OrganizationAccount organization = createOrganization(true);
        Event event = createEvent(organization, false);
        when(organizationAccountRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(ticketClassRepository.findAllBySalesRound_Event_IdOrderBySalesRoundStartTimeAscIdAsc(event.getId())).thenReturn(List.of());

        InvalidStateException exception = assertThrows(InvalidStateException.class,
                () -> eventService.publishEvent(organization.getId(), event.getId()));

        assertFalse(event.getPublished());
        assertEquals(
                "Event must have at least one ticket class before publishing",
                exception.getMessage()
        );
        verify(eventRepository, never()).save(event);
    }

    @Test
    void publishEvent_shouldRejectWhenEventHasNoAvailableSeats() {
        OrganizationAccount organization = createOrganization(true);
        Event event = createEvent(organization, false);
        when(organizationAccountRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(ticketClassRepository.findAllBySalesRound_Event_IdOrderBySalesRoundStartTimeAscIdAsc(event.getId())).thenReturn(List.of(TicketClass.builder().id(30L).build()));
        when(seatRepository.findAllByEventId(event.getId())).thenReturn(List.of(Seat.builder().id(40L).associatedTicket(Ticket.builder().id(50L).build()).build()));

        InvalidStateException exception = assertThrows(InvalidStateException.class,
                () -> eventService.publishEvent(organization.getId(), event.getId()));

        assertFalse(event.getPublished());
        assertEquals(
                "Event must have at least one available seat before publishing",
                exception.getMessage()
        );
        verify(eventRepository, never()).save(event);
    }

    @Test
    void publishEvent_shouldPublishWhenEventHasTicketClassAndAvailableSeat() {
        OrganizationAccount organization = createOrganization(true);
        Event event = createEvent(organization, false);
        when(organizationAccountRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(ticketClassRepository.findAllBySalesRound_Event_IdOrderBySalesRoundStartTimeAscIdAsc(event.getId())).thenReturn(List.of(TicketClass.builder().id(30L).build()));
        when(seatRepository.findAllByEventId(event.getId())).thenReturn(List.of(Seat.builder().id(40L).build()));

        eventService.publishEvent(organization.getId(), event.getId());

        assertTrue(event.getPublished());
        verify(eventRepository).save(event);
        verify(searchService).indexEvent(event);
    }

    private OrganizationAccount createOrganization(boolean verified) {
        return OrganizationAccount.builder()
                .id(10L)
                .name("Test Org")
                .email("org@example.com")
                .passwordHash("hashed")
                .verified(verified)
                .build();
    }

    private Event createEvent(OrganizationAccount organization, boolean published) {
        return Event.builder()
                .id(20L)
                .name("Test Event")
                .organization(organization)
                .published(published)
                .isOnlineEvent(false)
                .venue("Venue")
                .address("Address")
                .dateTime(Instant.now().plusSeconds(3600))
                .build();
    }
}
