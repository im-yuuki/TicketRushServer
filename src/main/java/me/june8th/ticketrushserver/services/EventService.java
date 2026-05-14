package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.*;
import me.june8th.ticketrushserver.repositories.*;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.Validator;
import me.june8th.ticketrushserver.types.SeatZoneData;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class EventService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final AccountRepository accountRepository;
    private final EventRepository eventRepository;
    private final SalesRoundRepository salesRoundRepository;
    private final SeatZoneRepository seatZoneRepository;
    private final SeatRowRepository seatRowRepository;
    private final SeatRepository seatRepository;
    private final TicketClassRepository ticketClassRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get an event by id.
     *
     * @param id event id
     * @return event
     */
    public Event getEvent(long id) {
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    /**
     * Create a new event for an organization.
     *
     * @param orgId organization id
     * @param eventName event name
     * @param venue venue name
     * @param address venue address
     * @param isOnlineEvent online flag
     * @param dateTime event time
     * @return created event
     */
    @NullMarked
    @Transactional
    public Event createEvent(long orgId, String eventName, String venue, String address, boolean isOnlineEvent, Instant dateTime) {
        Validator.create()
                .validateName(eventName)
                .validateNotBlank(venue)
                .validateNotBlank(address)
                .validateFutureDate(Date.from(dateTime))
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = Event.builder()
                .name(eventName)
                .organization(org)
                .isOnlineEvent(isOnlineEvent)
                .venue(venue)
                .address(address)
                .dateTime(dateTime)
                .build();
        Event savedEvent = eventRepository.save(event);
        logger.debug("Successfully created event with ID: {} for organization ID: {}", savedEvent.getId(), orgId);
        return savedEvent;
    }

    /**
     * Update basic event information before publish.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param patch patch data
     * @return updated event
     */
    @NullMarked
    @Transactional
    public Event updateEventBasicInformation(long orgId, long eventId, Event patch) {
        Validator.create()
                .validateName(patch.getName())
                .validateNotBlank(patch.getVenue())
                .validateNotBlank(patch.getAddress())
                .validateFutureDate(Date.from(patch.getDateTime()))
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getOrganizationEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        throw new NotImplementedException(); // TODO:
        // PatchUtils.applyPatch(event, patch, Patchable.class);
        // Event updatedEvent = eventRepository.save(event);
        // logger.debug("Successfully updated basic information for event ID: {}", eventId);
        // return updatedEvent;
    }

    /**
     * Add a sales round to an event.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param roundName round name
     * @param startTime start time
     * @param endTime end time
     * @param maxTicketsPerPurchase max tickets per purchase
     * @return created sales round
     */
    @NullMarked
    @Transactional
    public SalesRound addSalesRound(long orgId, long eventId, String roundName, Instant startTime, Instant endTime, int maxTicketsPerPurchase) {
        Validator.create()
                .validateName(roundName)
                .validateFutureDate(Date.from(startTime))
                .validateFutureDate(Date.from(endTime))
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getOrganizationEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        SalesRound salesRound = SalesRound.builder()
                .name(roundName)
                .event(event)
                .startTime(startTime)
                .endTime(endTime)
                .maxTicketsPerPurchase(maxTicketsPerPurchase)
                .build();
        SalesRound savedSalesRound = salesRoundRepository.save(salesRound);
        logger.debug("Successfully added sales round with ID: {} to event ID: {}", savedSalesRound.getId(), eventId);
        return savedSalesRound;
    }

    /**
     * Update a sales round for an event.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param roundId sales round id
     * @param patch patch data
     * @return updated sales round
     */
    @NullMarked
    @Transactional
    public SalesRound updateSalsesRound(long orgId, long eventId, long roundId, SalesRound patch) {
        Validator.create()
                .validateName(patch.getName())
                .validateFutureDate(Date.from(patch.getStartTime()))
                .validateFutureDate(Date.from(patch.getEndTime()))
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getOrganizationEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        SalesRound salesRound = salesRoundRepository.findById(roundId).orElseThrow(
                () -> new ResourceNotFoundException("Sales round not found")
        );
        if (!salesRound.getEvent().equals(event)) throw new InvalidStateException("Sales round does not belong to this event");
        throw new NotImplementedException(); // TODO:
        // PatchUtils.applyPatch(salesRound, patch, Patchable.class);
        // SalesRound updatedSalesRound = salesRoundRepository.save(salesRound);
        // logger.debug("Successfully updated sales round with ID: {} for event ID: {}", updatedSalesRound.getId(), eventId);
        // return updatedSalesRound;
    }

    /**
     * Delete a sales round from an event.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param roundId sales round id
     */
    @NullMarked
    @Transactional
    public void deleteSalesRound(long orgId, long eventId, long roundId) {
        Event event = getOrganizationEvent(getOrganizationAccount(orgId), eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        SalesRound salesRound = salesRoundRepository.findById(roundId).orElseThrow(
                () -> new ResourceNotFoundException("Sales round not found")
        );
        if (!salesRound.getEvent().equals(event)) throw new InvalidStateException("Sales round does not belong to this event");
        salesRoundRepository.delete(salesRound);
        logger.debug("Successfully deleted sales round with ID: {} from event ID: {}", roundId, eventId);
    }

    /**
     * Create seat zones, rows, and seats for an event.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param data seat layout data
     * @return created seat zone
     */
    @NullMarked
    @Transactional
    public SeatZone createSeatZone(long orgId, long eventId, SeatZoneData data) {
        Validator.create()
                .validateName(data.name())
                .validateNaturalNumber(data.positionX())
                .validateNaturalNumber(data.positionY())
                .throwExceptionIfInvalid();
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getOrganizationEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        long totalSeats = 0L;
        SeatZone seatZone = SeatZone.builder()
                .name(data.name())
                .positionX(data.positionX())
                .positionY(data.positionY())
                .event(event) // Added event to seatZone builder
                .build();
        seatZone = seatZoneRepository.save(seatZone);
        for (SeatZoneData.SeatRowView row : data.rows()) {
            Validator.create()
                    .validateNotBlank(row.label())
                    .validateNaturalNumber(row.index())
                    .throwExceptionIfInvalid();
            SeatRow seatRow = SeatRow.builder()
                    .seatZone(seatZone)
                    .index(row.index())
                    .label(row.label())
                    .build();
            seatRow = seatRowRepository.save(seatRow);
            for (SeatZoneData.SeatRowView.SeatView seat : row.seats()) {
                Validator.create()
                        .validateNaturalNumber(seat.index())
                        .validateNaturalNumber(seat.number())
                        .throwExceptionIfInvalid();
                Seat seatEntity = Seat.builder()
                        .seatRow(seatRow)
                        .index(seat.index())
                        .number(seat.number())
                        .build();
                seatRepository.save(seatEntity);
                totalSeats++;
            }
        }
        logger.debug(
                "Created {} seats for seat zone {} (ID: {}), event {} (ID: {})",
                totalSeats, seatZone.getName(), seatZone.getId(), seatZone.getEvent().getName(), seatZone.getEvent().getId()
        );
        return seatZone;
    }

    /**
     * Delete a seat zone from an event.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param zoneId seat zone id
     */
    @NullMarked
    @Transactional
    public void deleteSeatZone(long orgId, long eventId, long zoneId) {
        Event event = getOrganizationEvent(getOrganizationAccount(orgId), eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        SeatZone seatZone = seatZoneRepository.findById(zoneId).orElseThrow(
                () -> new ResourceNotFoundException("Seat zone not found")
        );
        if (!seatZone.getEvent().equals(event)) throw new InvalidStateException("Seat zone does not belong to this event");
        seatZoneRepository.delete(seatZone);
        logger.debug("Successfully deleted seat zone with ID: {} from event ID: {}", zoneId, eventId);
    }

    /**
     * Create a ticket class for an event.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param name ticket class name
     * @param description ticket class description
     * @param price ticket class price
     * @param salesRoundId sales round id
     * @param seatZoneId seat zone id
     * @return created ticket class
     */
    @NullMarked
    @Transactional
    public TicketClass createTicketClass(long orgId, long eventId, String name, String description, long price, long salesRoundId, long seatZoneId) {
        Validator.create()
                .validateName(name)
                .validateNotBlank(description)
                .validateNaturalNumber(price)
                .throwExceptionIfInvalid();
        Event event = getOrganizationEvent(getOrganizationAccount(orgId), eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        SalesRound salesRound = salesRoundRepository.findById(salesRoundId).orElseThrow(
                () -> new ResourceNotFoundException("Sales round not found")
        );
        SeatZone seatZone = seatZoneRepository.findById(seatZoneId).orElseThrow(
                () -> new ResourceNotFoundException("Seat zone not found")
        );
        if (!salesRound.getEvent().equals(event)) throw new InvalidStateException("Sales round does not belong to this event");
        if (!seatZone.getEvent().equals(event)) throw new InvalidStateException("Seat zone does not belong to this event");
        TicketClass ticketClass = TicketClass.builder()
                .name(name)
                .description(description)
                .price(price)
                .salesRound(salesRound)
                .seatZone(seatZone)
                .build();
        TicketClass savedTicketClass = ticketClassRepository.save(ticketClass);
        logger.debug("Successfully created ticket class with ID: {} for event ID: {}", savedTicketClass.getId(), eventId);
        return savedTicketClass;
    }

    /**
     * Delete a ticket class from an event.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param ticketClassId ticket class id
     */
    @NullMarked
    @Transactional
    public void deleteTicketClass(long orgId, long eventId, long ticketClassId) {
        Event event = getOrganizationEvent(getOrganizationAccount(orgId), eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be updated");
        TicketClass ticketClass = ticketClassRepository.findById(ticketClassId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket class not found")
        );
        if (!ticketClass.getSalesRound().getEvent().equals(event)) throw new InvalidStateException("Ticket class does not belong to this event");
        ticketClassRepository.delete(ticketClass);
        logger.debug("Successfully deleted ticket class with ID: {} from event ID: {}", ticketClassId, eventId);
    }

    /**
     * Publish an event.
     *
     * @param orgId organization id
     * @param eventId event id
     */
    @NullMarked
    @Transactional
    public void publishEvent(long orgId, long eventId) {
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getOrganizationEvent(org, eventId);
        if (event.getPublished()) throw new InvalidStateException("Event is already published");
        event.setPublished(true);
        eventRepository.save(event);
        logger.debug("Successfully published event with ID: {}", eventId);
    }

    /**
     * Add a staff account for a published event.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param name staff name
     * @param email staff email
     * @param password staff password
     * @return created staff account
     */
    @NullMarked
    @Transactional
    public EventStaffAccount addEventStaffAccount(long orgId, long eventId, String name, String email, String password) {
        Validator.create()
                .validateName(name)
                .validateEmail(email)
                .validatePassword(password)
                .throwExceptionIfInvalid();
        Event event = getOrganizationEvent(getOrganizationAccount(orgId), eventId);
        if (!event.getPublished()) throw new ForbiddenException("Staff accounts is only available for published events");
        if (accountRepository.existsByEmail(email)) {
            throw new ResourceConflictException("This email is already registered");
        }
        EventStaffAccount eventStaffAccount = EventStaffAccount.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .event(event)
                .build();
        eventStaffAccount = accountRepository.save(eventStaffAccount);
        logger.debug("Successfully added event staff account with ID: {} to event ID: {}", eventStaffAccount.getId(), eventId);
        return eventStaffAccount;
    }

    /**
     * Delete an event staff account.
     *
     * @param orgId organization id
     * @param eventId event id
     * @param staffId staff account id
     */
    public void deleteEventStaffAccount(long orgId, long eventId, long staffId) {
        Event event = getOrganizationEvent(getOrganizationAccount(orgId), eventId);
        if (event.getPublished()) throw new ForbiddenException("Staff accounts is only available for published events");
        Account staffAccount = accountRepository.findById(staffId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (staffAccount instanceof EventStaffAccount eventStaffAccount) {
            if (!event.equals(eventStaffAccount.getEvent()))
                throw new InvalidStateException("Account is not associated with this event");
            accountRepository.delete(staffAccount);
            logger.debug("Successfully deleted event staff account with ID: {} from event ID: {}", staffId, eventId);
        }
        throw new InvalidStateException("Account is not an event staff account");
    }

    /**
     * Delete an event before publish.
     *
     * @param orgId organization id
     * @param eventId event id
     */
    @NullMarked
    @Transactional
    public void deleteEvent(long orgId, long eventId) {
        OrganizationAccount org = getOrganizationAccount(orgId);
        Event event = getOrganizationEvent(org, eventId);
        if (event.getPublished()) throw new ForbiddenException("Published event can't be deleted");
        eventRepository.delete(event);
        logger.debug("Successfully deleted event with ID: {}", eventId);
    }

    private OrganizationAccount getOrganizationAccount(long id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (Role.ORGANIZATION.equals(account.getRole())) {
            logger.trace("Successfully retrieved organization account with ID: {}", id);
            return (OrganizationAccount) account;
        }
        throw new InvalidStateException("Account is not an organization");
    }

    private Event getOrganizationEvent(OrganizationAccount org, long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("Event not found")
        );
        if (org.equals(event.getOrganization())) {
            logger.trace("Successfully retrieved event ID: {} for organization ID: {}", eventId, org.getId());
            return event;
        }
        throw new InvalidStateException("The event does not belong to this organization");
    }

}
