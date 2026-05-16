package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.services.FeedService;
import me.june8th.ticketrushserver.services.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collection;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
public class FeedsController {

    private final FeedService feedService;
    private final StorageService storageService;

    @GetMapping("/promoted")
    public ResponseEntity<Collection<BasicEventInfo>> getPromotedEvents() {
        return ResponseEntity.ok(feedService.getPromotedEvents().stream().map(event -> new BasicEventInfo(storageService, event)).toList());
    }

    @GetMapping("/trending")
    public ResponseEntity<Collection<BasicEventInfo>> getTrendingEvents() {
        return ResponseEntity.ok(feedService.getTrendingEvents().stream().map(event -> new BasicEventInfo(storageService, event)).toList());
    }

    @GetMapping("/recommendeds")
    public ResponseEntity<Collection<BasicEventInfo>> getRecommendedEvents(@AuthenticationPrincipal Long id) {
        return ResponseEntity.ok(feedService.getRecommendedEvents(id).stream().map(event -> new BasicEventInfo(storageService, event)).toList());
    }

    public record BasicEventInfo(long id, String name, String imageUrl, Instant dateTime, String venue) {

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

}
