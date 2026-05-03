package me.june8th.ticketrushserver.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class PublicEventController {

    @GetMapping("/{id}")
    public String getEventInfo(@PathVariable String id) {
        return null;
    }

    @GetMapping("/interested")
    public String getInterestedEvents() {
        return null;
    }

    @PutMapping("/interested/{id}")
    public String markInterested(@PathVariable String id) {
        return null;
    }

    @DeleteMapping("/interested/{id}")
    public String unmarkInterested(@PathVariable String id) {
        return null;
    }

}
