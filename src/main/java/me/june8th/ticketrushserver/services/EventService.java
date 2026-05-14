package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.*;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.repositories.EventRepository;
import me.june8th.ticketrushserver.repositories.SalesRoundRepository;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.PatchUtils;
import me.june8th.ticketrushserver.utils.Validator;
import me.june8th.ticketrushserver.views.Patchable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class EventService {

    private final AccountRepository accountRepository;
    private final EventRepository eventRepository;
    private final SalesRoundRepository salesRoundRepository;

    public Event getEvent(long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Event not found")
        );
    }

    public Event createEvent(long orgId, String eventName, String venue, String address, Instant dateTime) {
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

    public Event updateEventBasicInformation(long orgId, long eventId, Event patch) {
        Validator.create()
                .validateName(patch.getName())
                .validateName(patch.getVenue())
                .validateName(patch.getAddress())
                .validateFutureDate(Date.from(patch.getDateTime()))
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        PatchUtils.applyPatch(event, patch, Patchable.class);
        return eventRepository.save(event);
    }

    public SalesRound addSalesRound(long orgId, long eventId, String roundName, Instant startTime, Instant endTime, int maxTicketsPerPurchase) {
        Validator.create()
                .validateName(roundName)
                .validateFutureDate(Date.from(startTime))
                .validateFutureDate(Date.from(endTime))
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        SalesRound salesRound = SalesRound.builder()
                .name(roundName)
                .event(event)
                .startTime(startTime)
                .endTime(endTime)
                .maxTicketsPerPurchase(maxTicketsPerPurchase)
                .build();
        return salesRoundRepository.save(salesRound);
    }

    public SalesRound updateSalsesRound(long orgId, long eventId, long roundId, SalesRound patch) {
        Validator.create()
                .validateName(patch.getName())
                .validateFutureDate(Date.from(patch.getStartTime()))
                .validateFutureDate(Date.from(patch.getEndTime()))
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        SalesRound salesRound = salesRoundRepository.findById(roundId).orElseThrow(
                () -> new ResourceNotFoundException("Sales round not found")
        );
        if (!salesRound.getEvent().equals(event)) throw new InvalidStateException("Sales round does not belong to this event");
        PatchUtils.applyPatch(salesRound, patch, Patchable.class);
        return salesRoundRepository.save(salesRound);
    }

    public void deleteSalesRound(long orgId, long eventId, long roundId) {
        Event event = getEvent(getOrganizationAccount(orgId), eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        SalesRound salesRound = salesRoundRepository.findById(roundId).orElseThrow(
                () -> new ResourceNotFoundException("Sales round not found")
        );
        if (!salesRound.getEvent().equals(event)) throw new InvalidStateException("Sales round does not belong to this event");
        salesRoundRepository.delete(salesRound);
    }



    public void publishEvent(long orgId, long eventId) {
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getEvent(org, eventId);
        if (event.getPublished()) throw new InvalidStateException("Event is already published");
        event.setPublished(true);
        eventRepository.save(event);
    }

    public void deleteEvent(long orgId, long eventId) {
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be deleted");
        eventRepository.delete(event);
    }

    private OrganizationAccount getOrganizationAccount(long id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (Role.ORGANIZATION.equals(account.getRole())) return (OrganizationAccount) account;
        throw new InvalidStateException("Account is not an organization");
    }

    private Event getEvent(OrganizationAccount org, long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("Event not found")
        );
        if (org.equals(event.getOrganization())) return event;
        throw new InvalidStateException("The event does not belong to this organization");
    }

}
