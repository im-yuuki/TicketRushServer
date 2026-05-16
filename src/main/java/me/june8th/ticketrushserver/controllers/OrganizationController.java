package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.EventService;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.types.CreateSeatZonePayload;
import me.june8th.ticketrushserver.types.ForbiddenException;
import me.june8th.ticketrushserver.types.OperationResult;
import me.june8th.ticketrushserver.types.UpdateEventPayload;
import me.june8th.ticketrushserver.types.UpdateOrganizationInfoPayload;
import me.june8th.ticketrushserver.types.UpdateSalesRoundData;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;

@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final AccountService accountService;
    private final EventService eventService;
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<FullOrganizationInfo> getInfo(@AuthenticationPrincipal long id) {
        if (accountService.getAccountData(id) instanceof OrganizationAccount org) {
            return ResponseEntity.ok(new FullOrganizationInfo(storageService, org));
        }
        throw new ForbiddenException("You do not have permission to access this resource");
    }

    @PatchMapping
    public ResponseEntity<OperationResult> updateInfo(@AuthenticationPrincipal long id, @RequestBody UpdateOrganizationInfoPayload payload) {
        accountService.updateOrganizationInfo(id, payload);
        return ResponseEntity.ok(OperationResult.success("Organization information updated successfully"));
    }

    @PutMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OperationResult> updateAvatar(@AuthenticationPrincipal long id, @RequestPart("file") MultipartFile file) throws IOException {
        accountService.updateAvatar(id, file);
        return ResponseEntity.ok(OperationResult.success("Avatar updated successfully"));
    }

    @PutMapping(path = "/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OperationResult> updateBanner(@AuthenticationPrincipal long id, @RequestPart("file") MultipartFile file) throws IOException {
        accountService.updateOrganizationBanner(id, file);
        return ResponseEntity.ok(OperationResult.success("Banner updated successfully"));
    }

    @GetMapping("/events")
    public ResponseEntity<Collection<BasicEventInfo>> getEvents(@AuthenticationPrincipal long id) {
        return ResponseEntity.ok(
                eventService.getAllOrganizationEvents(id).stream()
                .map(event -> new BasicEventInfo(storageService, event))
                .toList()
        );
    }

    @PostMapping("/events")
    public ResponseEntity<OperationResult> createEvent(@AuthenticationPrincipal long id, @RequestBody CreateEventPayload payload) {
        Event event = eventService.createEvent(id, payload.name(), payload.venue(), payload.address(), payload.isOnlineEvent(), payload.dateTime());
        return ResponseEntity.ok(OperationResult.success("Event created successfully", event.getId()));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<FullEventInfo> getEventDetails(@AuthenticationPrincipal long id, @PathVariable long eventId) {
        return ResponseEntity.ok(new FullEventInfo(storageService, eventService.getOrganizationEvent(id, eventId)));
    }

    @PatchMapping("/events/{eventId}")
    public ResponseEntity<OperationResult> updateEvent(@AuthenticationPrincipal long id, @PathVariable long eventId, @RequestBody UpdateEventPayload payload) {
        eventService.updateEventBasicInformation(id, eventId, payload);
        return ResponseEntity.ok(OperationResult.success("Event updated successfully"));
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<OperationResult> deleteEvent(@AuthenticationPrincipal long id, @PathVariable long eventId) {
        eventService.deleteEvent(id, eventId);
        return ResponseEntity.ok(OperationResult.success("Event deleted successfully"));
    }

    @PostMapping("/events/{eventId}/publish")
    public ResponseEntity<OperationResult> publishEvent(@AuthenticationPrincipal long id, @PathVariable long eventId) {
        eventService.publishEvent(id, eventId);
        return ResponseEntity.ok(OperationResult.success("Event published successfully"));
    }

    @PutMapping(path = "/events/{eventId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OperationResult> updateEventBanner(@AuthenticationPrincipal long id, @PathVariable long eventId, @RequestPart("file") MultipartFile file) throws IOException {
        eventService.updateEventBanner(id, eventId, file);
        return ResponseEntity.ok(OperationResult.success("Event banner updated successfully"));
    }

    @PostMapping("/events/{eventId}/sales-rounds")
    public ResponseEntity<OperationResult> addSalesRound(@AuthenticationPrincipal long id, @PathVariable long eventId, @RequestBody AddSalesRoundPayload payload) {
        var salesRound = eventService.addSalesRound(id, eventId, payload.name(), payload.startTime(), payload.endTime(), payload.maxTicketsPerPurchase());
        return ResponseEntity.ok(OperationResult.success("Sales round created successfully", salesRound.getId()));
    }

    @PatchMapping("/events/{eventId}/sales-rounds/{roundId}")
    public ResponseEntity<OperationResult> updateSalesRound(@AuthenticationPrincipal long id, @PathVariable long eventId, @PathVariable long roundId, @RequestBody UpdateSalesRoundData payload) {
        var updated = eventService.updateSalsesRound(id, eventId, roundId, payload);
        return ResponseEntity.ok(OperationResult.success("Sales round updated successfully", updated.getId()));
    }

    @DeleteMapping("/events/{eventId}/sales-rounds/{roundId}")
    public ResponseEntity<OperationResult> deleteSalesRound(@AuthenticationPrincipal long id, @PathVariable long eventId, @PathVariable long roundId) {
        eventService.deleteSalesRound(id, eventId, roundId);
        return ResponseEntity.ok(OperationResult.success("Sales round deleted successfully"));
    }

    @PostMapping("/events/{eventId}/seat-zones")
    public ResponseEntity<OperationResult> createSeatZone(@AuthenticationPrincipal long id, @PathVariable long eventId, @RequestBody CreateSeatZonePayload payload) {
        var seatZone = eventService.createSeatZone(id, eventId, payload);
        return ResponseEntity.ok(OperationResult.success("Seat zone created successfully", seatZone.getId()));
    }

    @DeleteMapping("/events/{eventId}/seat-zones/{zoneId}")
    public ResponseEntity<OperationResult> deleteSeatZone(@AuthenticationPrincipal long id, @PathVariable long eventId, @PathVariable long zoneId) {
        eventService.deleteSeatZone(id, eventId, zoneId);
        return ResponseEntity.ok(OperationResult.success("Seat zone deleted successfully"));
    }

    @PostMapping("/events/{eventId}/ticket-classes")
    public ResponseEntity<OperationResult> createTicketClass(@AuthenticationPrincipal long id, @PathVariable long eventId, @RequestBody CreateTicketClassPayload payload) {
        var ticketClass = eventService.createTicketClass(id, eventId, payload.name(), payload.description(), payload.price(), payload.salesRoundId(), payload.seatZoneId());
        return ResponseEntity.ok(OperationResult.success("Ticket class created successfully", ticketClass.getId()));
    }

    @DeleteMapping("/events/{eventId}/ticket-classes/{ticketClassId}")
    public ResponseEntity<OperationResult> deleteTicketClass(@AuthenticationPrincipal long id, @PathVariable long eventId, @PathVariable long ticketClassId) {
        eventService.deleteTicketClass(id, eventId, ticketClassId);
        return ResponseEntity.ok(OperationResult.success("Ticket class deleted successfully"));
    }

    @PostMapping("/events/{eventId}/staff")
    public ResponseEntity<OperationResult> addEventStaffAccount(@AuthenticationPrincipal long id, @PathVariable long eventId, @RequestBody AddEventStaffPayload payload) {
        var staffAccount = eventService.addEventStaffAccount(id, eventId, payload.name(), payload.email(), payload.password());
        return ResponseEntity.ok(OperationResult.success("Event staff account created successfully", staffAccount.getId()));
    }

    @DeleteMapping("/events/{eventId}/staff/{staffId}")
    public ResponseEntity<OperationResult> deleteEventStaffAccount(@AuthenticationPrincipal long id, @PathVariable long eventId, @PathVariable long staffId) {
        eventService.deleteEventStaffAccount(id, eventId, staffId);
        return ResponseEntity.ok(OperationResult.success("Event staff account deleted successfully"));
    }

    public record FullOrganizationInfo(
            long id,
            String name,
            String email,
            Instant createdAt,
            Instant updatedAt,
            boolean verified,
            String description,
            String aliasName,
            String avatarUrl,
            String bannerUrl,
            String websiteUrl
    ) {

        public FullOrganizationInfo(StorageService storageService, OrganizationAccount org) {
            this(
                    org.getId(),
                    org.getName(),
                    org.getEmail(),
                    org.getCreatedAt(),
                    org.getUpdatedAt(),
                    org.getVerified(),
                    org.getDescription(),
                    org.getAliasName(),
                    storageService.generatePresignedUrl(org.getAvatarKey()),
                    storageService.generatePresignedUrl(org.getBannerKey()),
                    org.getWebsiteUrl()
            );
        }

    }

    public record BasicEventInfo(
            long id,
            String name,
            String bannerUrl,
            Instant dateTime,
            String venue
    ) {
        public BasicEventInfo(StorageService storageService, Event event) {
            this(
                    event.getId(),
                    event.getName(),
                    storageService.generatePresignedUrl(event.getBannerKey()),
                    event.getDateTime(),
                    event.getVenue()
            );
        }
    }

    public record FullEventInfo(
            long id,
            String name,
            String description,
            boolean published,
            boolean isOnlineEvent,
            String venue,
            String address,
            Instant dateTime,
            String bannerUrl,
            Instant createdAt,
            Instant updatedAt
    ) {
        public FullEventInfo(StorageService storageService, Event event) {
            this(
                    event.getId(),
                    event.getName(),
                    event.getDescription(),
                    event.getPublished(),
                    event.isOnlineEvent(),
                    event.getVenue(),
                    event.getAddress(),
                    event.getDateTime(),
                    storageService.generatePresignedUrl(event.getBannerKey()),
                    event.getCreatedAt(),
                    event.getUpdatedAt()
            );
        }
    }

    public record CreateEventPayload(
            String name,
            String description,
            boolean isOnlineEvent,
            String venue,
            String address,
            Instant dateTime
    ) {}

    public record AddSalesRoundPayload(
            String name,
            Instant startTime,
            Instant endTime,
            int maxTicketsPerPurchase
    ) {}

    public record CreateTicketClassPayload(
            String name,
            String description,
            long price,
            long salesRoundId,
            long seatZoneId
    ) {}

    public record AddEventStaffPayload(
            String name,
            String email,
            String password
    ) {}

}
