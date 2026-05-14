package me.june8th.ticketrushserver.services;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.repositories.EventRepository;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.Validator;
import me.june8th.ticketrushserver.views.Patchable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;

    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Event not found")
        );
    }

    public Event createEvent(Long orgId, String eventName, String venue, String address, Instant dateTime) {
        Validator.create()
                .validateName(eventName)
                .validateName(venue)
                .validateName(address)
                .validateFutureDate(Date.from(dateTime))
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = Event.builder()
                .name(eventName)
                .organization(org)
                .venue(venue)
                .address(address)
                .dateTime(dateTime)
                .build();
        return eventRepository.save(event);
    }

    public Event updateEventBasicInformation(Long orgId, Long eventId, @JsonView(Patchable.class) Event patch) {
        Validator.create()
                .validateName(patch.getName())
                .validateName(patch.getVenue())
                .validateName(patch.getAddress())
                .validateFutureDate(Date.from(patch.getDateTime()))
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        // TODO: patch object
        return eventRepository.save(event);
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
        if (event.getPublished()) throw new ForbiddenException("Published event can't be deleted");
        eventRepository.delete(event);
    }

    private OrganizationAccount getOrganizationAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (Role.ORGANIZATION.equals(account.getRole())) return (OrganizationAccount) account;
        throw new InvalidStateException("Account is not an organization");
    }

    private Event getEvent(OrganizationAccount org, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("Event not found")
        );
        if (org.equals(event.getOrganization())) return event;
        throw new InvalidStateException("The event does not belong to this organization");
    }

}
