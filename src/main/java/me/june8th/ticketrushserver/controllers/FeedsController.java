package me.june8th.ticketrushserver.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feeds")
public class FeedsController {

    @GetMapping("/promoted")
    public ResponseEntity<?> getPromotedEvents() {
        return null;
    }

    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingEvents() {
        return null;
    }

    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedEvents() {
        return null;
    }

}
