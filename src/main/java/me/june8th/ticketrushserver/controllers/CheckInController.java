package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.types.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/checkin")
public class CheckInController {

    @PostMapping
    public ResponseEntity<CheckInResult> checkIn(@AuthenticationPrincipal long staffId, @RequestBody CheckInPayload payload) {
        throw new NotImplementedException();
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
    ) {}

}
