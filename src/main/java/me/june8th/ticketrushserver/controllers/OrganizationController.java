package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.EventService;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.types.ForbiddenException;
import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.types.OperationResult;
import me.june8th.ticketrushserver.types.UpdateOrganizationInfoPayload;
import me.june8th.ticketrushserver.types.UpdateEventPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;

@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final AccountService accountService;
    private final EventService eventService;
    private final StorageService storageService;

    @GetMapping("/info")
    public ResponseEntity<FullOrganizationInfo> getInfo(@AuthenticationPrincipal long id) {
        if (accountService.getAccountData(id) instanceof OrganizationAccount org) {
            return ResponseEntity.ok(new FullOrganizationInfo(storageService, org));
        }
        throw new ForbiddenException("You do not have permission to access this resource");
    }

    @PutMapping("/info")
    public ResponseEntity<OperationResult> updateInfo(@AuthenticationPrincipal long id, @RequestBody UpdateOrganizationInfoPayload payload) {
        accountService.updateOrganizationInfo(id, payload);
        return ResponseEntity.ok(OperationResult.success("Organization information updated successfully"));
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

    @PutMapping("/events/{eventId}")
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

}
