package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Ticket;
import me.june8th.ticketrushserver.services.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ResponseEntity<CheckInResult> checkIn(@AuthenticationPrincipal long staffId, @RequestBody CheckInPayload payload) {
        Instant now = Instant.now();
        Ticket ticket = checkInService.checkIn(
                staffId, payload.eventId(), payload.ticketId(),
                payload.userId(), payload.ticketSecretCode(), now
        );
        return ResponseEntity.ok(new CheckInResult(now.equals(ticket.getCheckedInAt()), ticket));
    }

    public record CheckInPayload(
            long eventId,
            long ticketId,
            long userId,
            String ticketSecretCode
    ) {}

    public record CheckInResult(
            // check-in result
            boolean ticketValid,
            Instant checkInTime,
            String checkInStaffName,
            // owner info
            String ticketOwnerName,
            String ticketOwnerEmail,
            String ticketOwnerPhoneNumber,
            // ticket info
            String salesRoundName,
            String ticketClassName,
            String seatZoneName,
            String seatRowLabel,
            int seatNumber
    ) {
        public CheckInResult(boolean ticketNotCheckedInBefore, Ticket ticket) {
            this(
                    ticketNotCheckedInBefore,
                    ticket.getCheckedInAt(),
                    ticket.getCheckInStaff().getName(),
                    ticket.getUser().getName(),
                    ticket.getUser().getEmail(),
                    ticket.getUser().getPhoneNumber(),
                    ticket.getTicketClass().getSalesRound().getName(),
                    ticket.getTicketClass().getName(),
                    ticket.getSeat().getSeatRow().getSeatZone().getName(),
                    ticket.getSeat().getSeatRow().getLabel(),
                    ticket.getSeat().getNumber()
            );
        }
    }

}
