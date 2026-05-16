package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.types.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collection;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
public class FeedsController {

    private final StorageService storageService;

    @GetMapping("/promoted")
    public ResponseEntity<Collection<BasicEventInfo>> getPromotedEvents() {
        throw new NotImplementedException();
    }

    @GetMapping("/trending")
    public ResponseEntity<Collection<BasicEventInfo>> getTrendingEvents() {
        throw new NotImplementedException();
    }

    @GetMapping("/recommendeds")
    public ResponseEntity<Collection<BasicEventInfo>> getRecommendedEvents() {
        throw new NotImplementedException();
    }

    public record BasicEventInfo(long id, String name, String imageUrl, Instant dateTime, String venue) {

        public BasicEventInfo(StorageService storageService, Event event) {
            String imageUrl = storageService.generatePresignedUrl(event.getBannerKey());
            this(event.getId(), event.getName(), imageUrl, event.getDateTime(), event.getVenue());
        }

    };

}
