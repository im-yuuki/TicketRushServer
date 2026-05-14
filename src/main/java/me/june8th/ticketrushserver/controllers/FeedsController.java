package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.types.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collection;

@RestController
@RequestMapping("/feeds")
public class FeedsController {

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

        public BasicEventInfo(Event event) {
            // TODO: image url generation logic
            this(event.getId(), event.getName(), event.getBannerKey(), event.getDateTime(), event.getVenue());
        }

    };

}
