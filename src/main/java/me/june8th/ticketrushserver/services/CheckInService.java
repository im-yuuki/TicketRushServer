package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.EventStaffAccount;
import me.june8th.ticketrushserver.data.Ticket;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.repositories.EventRepository;
import me.june8th.ticketrushserver.repositories.TicketRepository;
import me.june8th.ticketrushserver.types.ForbiddenException;
import me.june8th.ticketrushserver.types.InvalidStateException;
import me.june8th.ticketrushserver.types.ResourceNotFoundException;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private static final Logger logger = LoggerFactory.getLogger(CheckInService.class);

    private final AccountRepository accountRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    @NullMarked
    @Transactional
    public Ticket checkIn(long eventId, long ticketId, long userId, long staffId, String ticketSecretCode, Instant checkInTime) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        Account staffAccount = accountRepository.findById(staffId).orElseThrow(() -> new ResourceNotFoundException("Staff account not found"));
        if (staffAccount instanceof EventStaffAccount eventStaffAccount) {
            if (eventStaffAccount.getEvent().getId() != eventId) {
                throw new ForbiddenException("Staff account does not belong to this event");
            }
            Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
            // perform checks
            if (ticket.getTicketClass().getSalesRound().getEvent().getId() != eventId) {
                throw new InvalidStateException("Invalid ticket");
            }
            if (!ticket.equals(ticket.getSeat().getAssociatedTicket())) {
                throw new InvalidStateException("Invalid ticket");
            }
            if (ticket.getUser().getId() != userId) {
                throw new InvalidStateException("Invalid ticket");
            }
            if (!ticket.getTicketSecretCode().equals(ticketSecretCode)) {
                throw new ForbiddenException("Invalid ticket code");
            }
            if (ticket.getCheckedInAt() == null) {
                ticket.setCheckedInAt(checkInTime);
                ticket.setCheckInStaff(eventStaffAccount);
                Ticket updatedTicket = ticketRepository.save(ticket);
                logger.info("Ticket {} checked in at {} by staff {}", ticketId, checkInTime, staffId);
                return updatedTicket;
            }
            else {
                logger.debug("Ticket {} already checked in at {} by staff {}", ticketId, ticket.getCheckedInAt(), staffId);
                return ticket;
            }
        }
        throw new ForbiddenException("Account does not have permission to perform this operation");
    }

}
