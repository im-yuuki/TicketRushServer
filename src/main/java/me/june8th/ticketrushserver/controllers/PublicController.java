package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.EventService;
import me.june8th.ticketrushserver.services.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final EventService eventService;
    private final AccountService accountService;
    private final StorageService storageService;

    @GetMapping("/event/{id}")
    public ResponseEntity<PublicEventInfo> getEventInfo(@PathVariable Long id) {
        Event event = eventService.getPublishedEvent(id);
        return ResponseEntity.ok(new PublicEventInfo(storageService, event));
    }

    @GetMapping("/org/{id}")
    public ResponseEntity<PublicOrganizationInfo> getOrganizationInfo(@PathVariable Long id) {
        OrganizationAccount organization = accountService.getPublicOrganizationById(id);
        long followerCount = accountService.getOrganizationFollowerCount(id);
        return ResponseEntity.ok(new PublicOrganizationInfo(storageService, organization, followerCount));
    }

    @GetMapping("/org/@{alias}")
    public ResponseEntity<PublicOrganizationInfo> getOrganizationInfoByAlias(@PathVariable String alias) {
        OrganizationAccount organization = accountService.getPublicOrganizationByAlias(alias);
        long followerCount = accountService.getOrganizationFollowerCount(organization.getId());
        return ResponseEntity.ok(new PublicOrganizationInfo(storageService, organization, followerCount));
    }

    public record PublicEventInfo(
            long id,
            String name,
            String description,
            boolean isOnlineEvent,
            String venue,
            String address,
            Instant dateTime,
            String bannerUrl,
            long organizationId,
            String organizationName,
            String organizationAlias
    ) {
        public PublicEventInfo(StorageService storageService, Event event) {
            this(
                    event.getId(),
                    event.getName(),
                    event.getDescription(),
                    event.isOnlineEvent(),
                    event.getVenue(),
                    event.getAddress(),
                    event.getDateTime(),
                    storageService.generatePresignedUrl(event.getBannerKey()),
                    event.getOrganization().getId(),
                    event.getOrganization().getName(),
                    event.getOrganization().getAliasName()
            );
        }
    }

    public record PublicOrganizationInfo(
            long id,
            String name,
            String description,
            String aliasName,
            String avatarUrl,
            String bannerUrl,
            String websiteUrl,
            boolean verified,
            long followerCount
    ) {
        public PublicOrganizationInfo(StorageService storageService, OrganizationAccount organization, long followerCount) {
            this(
                    organization.getId(),
                    organization.getName(),
                    organization.getDescription(),
                    organization.getAliasName(),
                    storageService.generatePresignedUrl(organization.getAvatarKey()),
                    storageService.generatePresignedUrl(organization.getBannerKey()),
                    organization.getWebsiteUrl(),
                    organization.getVerified(),
                    followerCount
            );
        }
    }

}
