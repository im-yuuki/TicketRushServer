package me.june8th.ticketrushserver.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event/{id}")
public class EventInfoController {

    @GetMapping
    public String getEventInfo(@PathVariable String id) {
        return null;
    }

    @PostMapping("/interested")
    public String markInterested(@PathVariable String id) {
        return null;
    }

}
