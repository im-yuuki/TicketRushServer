package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.types.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feeds")
public class FeedsController {

    @GetMapping("/promoted")
    public ResponseEntity<?> getPromotedEvents() {
        throw new NotImplementedException();
    }

    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingEvents() {
        throw new NotImplementedException();
    }

    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedEvents() {
        throw new NotImplementedException();
    }

}
