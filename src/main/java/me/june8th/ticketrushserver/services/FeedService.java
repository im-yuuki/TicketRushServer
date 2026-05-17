package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.database.EventRepository;
import me.june8th.ticketrushserver.database.FollowRepository;
import me.june8th.ticketrushserver.database.TicketRepository;
import me.june8th.ticketrushserver.database.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FeedService {

    private static final int PROMOTED_LIMIT = 5;
    private static final int DEFAULT_LIMIT = 20;

    private final EventRepository eventRepository;
    private final FollowRepository followRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public List<Event> getPromotedEvents() {
        List<Event> upcomingEvents = getUpcomingPublishedEvents(Instant.now());
        Collections.shuffle(upcomingEvents);
        return limit(upcomingEvents, PROMOTED_LIMIT);
    }

    public List<Event> getTrendingEvents() {
        Instant now = Instant.now();
        List<Event> upcomingEvents = getUpcomingPublishedEvents(now);
        return buildTrendingEvents(now, upcomingEvents, DEFAULT_LIMIT);
    }

    public List<Event> getRecommendedEvents(Long accountId) {
        Instant now = Instant.now();
        List<Event> upcomingEvents = getUpcomingPublishedEvents(now);
        if (accountId == null) {
            return buildTrendingEvents(now, upcomingEvents, DEFAULT_LIMIT);
        }

        UserAccount user = userRepository.findById(accountId).orElse(null);
        if (user == null) {
            return buildTrendingEvents(now, upcomingEvents, DEFAULT_LIMIT);
        }

        LinkedHashMap<Long, Event> selectedEvents = new LinkedHashMap<>();
        Set<Long> followedOrganizationIds = followRepository.findAllByFollowerOrderByAtDescIdDesc(user).stream()
                .map(follow -> follow.getOrganization().getId())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        addRelevantOrganizationEvents(selectedEvents, upcomingEvents, followedOrganizationIds, DEFAULT_LIMIT);

        Set<Long> purchasedOrganizationIds = new LinkedHashSet<>(ticketRepository.findPurchasedOrganizationIdsByUserId(user.getId()));
        addRelevantOrganizationEvents(selectedEvents, upcomingEvents, purchasedOrganizationIds, DEFAULT_LIMIT);

        addEvents(selectedEvents, buildTrendingEvents(now, upcomingEvents, DEFAULT_LIMIT), DEFAULT_LIMIT);
        addRandomEvents(selectedEvents, upcomingEvents, DEFAULT_LIMIT);
        return limit(selectedEvents.values(), DEFAULT_LIMIT);
    }

    private List<Event> buildTrendingEvents(Instant now, List<Event> upcomingEvents, int limit) {
        if (upcomingEvents.isEmpty()) {
            return List.of();
        }

        LinkedHashMap<Long, Event> selectedEvents = new LinkedHashMap<>();
        LinkedHashMap<Long, Event> eventById = new LinkedHashMap<>();
        for (Event event : upcomingEvents) {
            eventById.put(event.getId(), event);
        }

        for (Long eventId : ticketRepository.findTrendingEventIds(now)) {
            Event event = eventById.get(eventId);
            if (event == null) continue;
            selectedEvents.putIfAbsent(event.getId(), event);
            if (selectedEvents.size() >= limit) {
                return limit(selectedEvents.values(), limit);
            }
        }

        addRandomEvents(selectedEvents, upcomingEvents, limit);
        return limit(selectedEvents.values(), limit);
    }

    private void addRelevantOrganizationEvents(LinkedHashMap<Long, Event> selectedEvents, List<Event> upcomingEvents, Set<Long> organizationIds, int limit) {
        if (organizationIds.isEmpty()) return;
        for (Event event : upcomingEvents) {
            if (!organizationIds.contains(event.getOrganization().getId())) continue;
            selectedEvents.putIfAbsent(event.getId(), event);
            if (selectedEvents.size() >= limit) {
                return;
            }
        }
    }

    private void addRandomEvents(LinkedHashMap<Long, Event> selectedEvents, List<Event> candidateEvents, int limit) {
        List<Event> shuffledEvents = new ArrayList<>(candidateEvents);
        Collections.shuffle(shuffledEvents);
        addEvents(selectedEvents, shuffledEvents, limit);
    }

    private void addEvents(LinkedHashMap<Long, Event> selectedEvents, List<Event> candidateEvents, int limit) {
        for (Event event : candidateEvents) {
            selectedEvents.putIfAbsent(event.getId(), event);
            if (selectedEvents.size() >= limit) {
                return;
            }
        }
    }

    private List<Event> getUpcomingPublishedEvents(Instant now) {
        return new ArrayList<>(eventRepository.findAllByPublishedTrueAndDateTimeAfterOrderByDateTimeAscIdAsc(now));
    }

    private List<Event> limit(Collection<Event> events, int limit) {
        return events.stream().limit(limit).toList();
    }

}
