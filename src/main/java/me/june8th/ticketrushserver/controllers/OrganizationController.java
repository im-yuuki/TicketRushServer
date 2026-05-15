package me.june8th.ticketrushserver.controllers;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.EventService;
import me.june8th.ticketrushserver.types.ForbiddenException;
import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.types.OperationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;

@RestController
@RequestMapping("/organization")
@RolesAllowed("ROLE_ORGANIZATION")
@RequiredArgsConstructor
public class OrganizationController {

    private final AccountService accountService;
    private final EventService eventService;

    @GetMapping("/info")
    public ResponseEntity<FullOrganizationInfo> getInfo(@AuthenticationPrincipal long id) {
        if (accountService.getAccountData(id) instanceof OrganizationAccount org) {
            return ResponseEntity.ok(new FullOrganizationInfo(org));
        }
        throw new ForbiddenException("You do not have permission to access this resource");
    }

    @PutMapping("/info")
    public ResponseEntity<OperationResult> updateInfo(@AuthenticationPrincipal long id, @RequestBody UpdateOrganizationInfoPayload payload) {
        throw new NotImplementedException();
    }

    @GetMapping("/events")
    public ResponseEntity<Collection<BasicEventInfo>> getEvents(@AuthenticationPrincipal long id) {
        OrganizationAccount org = eventService.getOrganizationAccount(id);
        return ResponseEntity.ok(eventService.getAllOrganizationEvents(org).stream().map(BasicEventInfo::new).toList());
    }

    @PostMapping("/events")
    public ResponseEntity<OperationResult> createEvent(@AuthenticationPrincipal long id, @RequestBody CreateEventPayload payload) {
        Event event = eventService.createEvent(id, payload.name(), payload.venue(), payload.address(), payload.isOnlineEvent(), payload.dateTime());
        return ResponseEntity.ok(OperationResult.success("Event created successfully", event.getId()));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<FullEventInfo> getEventDetails(@AuthenticationPrincipal long id, @PathVariable long eventId) {
        OrganizationAccount org = eventService.getOrganizationAccount(id);
        return ResponseEntity.ok(new FullEventInfo(eventService.getOrganizationEvent(org, eventId)));
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<OperationResult> updateEvent(@AuthenticationPrincipal long id, @PathVariable long eventId) {
        throw new NotImplementedException();
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

        public FullOrganizationInfo(OrganizationAccount org) {
            this(
                    org.getId(),
                    org.getName(),
                    org.getEmail(),
                    org.getCreatedAt(),
                    org.getUpdatedAt(),
                    org.getVerified(),
                    org.getDescription(),
                    org.getAliasName(),
                    org.getAvatarKey(), // TODO: avatar url generation logic
                    org.getBannerKey(), // TODO: banner url generation logic
                    org.getWebsiteUrl()
            );
        }

    }

    public record UpdateOrganizationInfoPayload(
            String description,
            String aliasName,
            String websiteUrl
    ) {}

    public record BasicEventInfo(
            long id,
            String name,
            String bannerUrl,
            Instant dateTime,
            String venue
    ) {
        public BasicEventInfo(Event event) {
            this(
                    event.getId(),
                    event.getName(),
                    event.getBannerKey(), // TODO: banner url generation logic
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
        public FullEventInfo(Event event) {
            this(
                    event.getId(),
                    event.getName(),
                    event.getDescription(),
                    event.getPublished(),
                    event.isOnlineEvent(),
                    event.getVenue(),
                    event.getAddress(),
                    event.getDateTime(),
                    event.getBannerKey(), // TODO: banner url generation logic
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
