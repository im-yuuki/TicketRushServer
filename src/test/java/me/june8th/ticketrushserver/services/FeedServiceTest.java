package me.june8th.ticketrushserver.services;

import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.Follow;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.database.EventRepository;
import me.june8th.ticketrushserver.database.FollowRepository;
import me.june8th.ticketrushserver.database.TicketRepository;
import me.june8th.ticketrushserver.database.UserRepository;
import me.june8th.ticketrushserver.types.Gender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedService feedService;

    @Test
    void promoted_shouldReturnAllAvailableUpcomingEventsWhenFewerThanLimit() {
        List<Event> upcomingEvents = List.of(
                createEvent(1L, 101L, 1),
                createEvent(2L, 102L, 2),
                createEvent(3L, 103L, 3)
        );
        when(eventRepository.findAllByPublishedTrueAndDateTimeAfterOrderByDateTimeAscIdAsc(any(Instant.class))).thenReturn(upcomingEvents);

        List<Event> result = feedService.getPromotedEvents();

        assertEquals(3, result.size());
        assertEquals(Set.of(1L, 2L, 3L), result.stream().map(Event::getId).collect(java.util.stream.Collectors.toSet()));
        verifyNoInteractions(followRepository, ticketRepository, userRepository);
    }

    @Test
    void trending_shouldPreferRankedEventsAndFillRemainingWithAvailableEvents() {
        List<Event> upcomingEvents = List.of(
                createEvent(1L, 101L, 1),
                createEvent(2L, 102L, 2),
                createEvent(3L, 103L, 3),
                createEvent(4L, 104L, 4)
        );
        when(eventRepository.findAllByPublishedTrueAndDateTimeAfterOrderByDateTimeAscIdAsc(any(Instant.class))).thenReturn(upcomingEvents);
        when(ticketRepository.findTrendingEventIds(any(Instant.class))).thenReturn(List.of(3L, 1L));

        List<Event> result = feedService.getTrendingEvents();

        assertEquals(4, result.size());
        assertIterableEquals(List.of(3L, 1L), result.subList(0, 2).stream().map(Event::getId).toList());
        assertTrue(result.stream().map(Event::getId).toList().containsAll(List.of(2L, 4L)));
    }

    @Test
    void recommended_shouldFallBackToTrendingForAnonymousUsers() {
        List<Event> upcomingEvents = List.of(
                createEvent(1L, 101L, 1),
                createEvent(2L, 102L, 2),
                createEvent(3L, 103L, 3)
        );
        when(eventRepository.findAllByPublishedTrueAndDateTimeAfterOrderByDateTimeAscIdAsc(any(Instant.class))).thenReturn(upcomingEvents);
        when(ticketRepository.findTrendingEventIds(any(Instant.class))).thenReturn(List.of(2L));

        List<Event> result = feedService.getRecommendedEvents(null);

        assertEquals(3, result.size());
        assertEquals(2L, result.getFirst().getId());
        verifyNoInteractions(userRepository, followRepository);
    }

    @Test
    void recommended_shouldPrioritizeFollowedAndPurchasedOrganizationsBeforeTrendingFallback() {
        UserAccount user = UserAccount.builder()
                .id(77L)
                .name("Test User")
                .email("user@example.com")
                .passwordHash("hashed")
                .birthDate(new Date(946684800000L))
                .gender(Gender.OTHER)
                .build();
        Event event1 = createEvent(1L, 101L, 1);
        Event event2 = createEvent(2L, 102L, 2);
        Event event3 = createEvent(3L, 103L, 3);
        Event event4 = createEvent(4L, 104L, 4);
        List<Event> upcomingEvents = List.of(event1, event2, event3, event4);

        when(eventRepository.findAllByPublishedTrueAndDateTimeAfterOrderByDateTimeAscIdAsc(any(Instant.class))).thenReturn(upcomingEvents);
        when(userRepository.findById(77L)).thenReturn(java.util.Optional.of(user));
        when(followRepository.findAllByFollowerOrderByAtDescIdDesc(user)).thenReturn(List.of(
                Follow.builder().organization(event2.getOrganization()).follower(user).build(),
                Follow.builder().organization(event4.getOrganization()).follower(user).build()
        ));
        when(ticketRepository.findPurchasedOrganizationIdsByUserId(77L)).thenReturn(List.of(103L));
        when(ticketRepository.findTrendingEventIds(any(Instant.class))).thenReturn(List.of(1L, 2L));

        List<Event> result = feedService.getRecommendedEvents(77L);

        assertIterableEquals(List.of(2L, 4L, 3L, 1L), result.stream().map(Event::getId).toList());
        verify(userRepository).findById(77L);
    }

    private Event createEvent(long eventId, long organizationId, long dateOffsetHours) {
        OrganizationAccount organization = OrganizationAccount.builder()
                .id(organizationId)
                .name("Organization " + organizationId)
                .email("org" + organizationId + "@example.com")
                .passwordHash("hashed")
                .verified(true)
                .build();
        return Event.builder()
                .id(eventId)
                .name("Event " + eventId)
                .organization(organization)
                .published(true)
                .isOnlineEvent(false)
                .venue("Venue " + eventId)
                .address("Address " + eventId)
                .dateTime(Instant.now().plusSeconds(dateOffsetHours * 3600))
                .bannerKey("events/" + eventId + ".png")
                .build();
    }

}
