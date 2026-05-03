package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.repositories.EventRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void createEvent(OrganizationAccount org, String eventName) {
    }

    public void updateEvent(OrganizationAccount org, Long eventId, String eventName) {

    }

    public void publishEvent(OrganizationAccount org, Long eventId) {

    }

    public void deleteEvent(OrganizationAccount org, Long eventId) {

    }

}
