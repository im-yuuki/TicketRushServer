package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.repositories.EventRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;

    public void createEvent(Long orgId, String eventName) {
    }

    public void updateEvent(Long orgId, Long eventId, String eventName) {

    }

    public void publishEvent(Long orgId, Long eventId) {

    }

    public void deleteEvent(Long orgId, Long eventId) {

    }

}
