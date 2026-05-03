package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.types.NotImplementedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class PublicEventController {

    @GetMapping("/{id}")
    public String getEventInfo(@PathVariable String id) {
        throw new NotImplementedException();
    }

    @GetMapping("/interested")
    public String getInterestedEvents() {
        throw new NotImplementedException();
    }

    @PutMapping("/interested/{id}")
    public String markInterested(@PathVariable String id) {
        throw new NotImplementedException();
    }

    @DeleteMapping("/interested/{id}")
    public String unmarkInterested(@PathVariable String id) {
        throw new NotImplementedException();
    }

}
