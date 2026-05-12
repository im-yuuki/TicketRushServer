package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.repositories.EventRepository;
import me.june8th.ticketrushserver.types.InvalidStateException;
import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.types.ResourceNotFoundException;
import me.june8th.ticketrushserver.types.Role;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;

    public Event createEvent(Long orgId, String eventName, String venue, String address, Instant dateTime) {
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = Event.builder()
                .name(eventName)
                .organization(org)
                .venue(venue)
                .address(address)
                .build();
        return eventRepository.save(event);
    }

    public void updateEvent(Long orgId, Long eventId) {
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getEvent(org, eventId);

        throw new NotImplementedException(); // TODO: implement
    }

    public void publishEvent(Long orgId, Long eventId) {
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getEvent(org, eventId);
        if (event.getPublished()) throw new InvalidStateException("Event is already published");
        event.setPublished(true);
        eventRepository.save(event);
    }

    public void deleteEvent(Long orgId, Long eventId) {
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getEvent(org, eventId);
        if (event.getPublished()) throw new InvalidStateException("Published event can't be deleted");
        eventRepository.delete(event);
    }

    public OrganizationAccount getOrganizationAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (Role.ORGANIZATION.equals(account.getRole())) return (OrganizationAccount) account;
        throw new InvalidStateException("Account is not an organization");
    }

    public Event getEvent(OrganizationAccount org, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("Event not found")
        );
        if (org.equals(event.getOrganization())) return event;
        throw new InvalidStateException("The event does not belong to this organization");
    }

}
