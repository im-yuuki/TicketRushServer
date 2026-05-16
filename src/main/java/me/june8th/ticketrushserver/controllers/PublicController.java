package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.services.EventService;
import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.types.OperationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PublicController {

    private final EventService eventService;

    @GetMapping("/event/{id}")
    public ResponseEntity<PublicEventInfo> getEventInfo(@PathVariable Long id) {
        Event event = eventService.getEvent(id);
        throw new NotImplementedException();
    }

    @GetMapping("/org/{id}")
    public ResponseEntity<PublicOrganizationInfo> getOrganizationInfo(@PathVariable Long id) {
        throw new NotImplementedException();
    }

    @GetMapping("/org/@{alias}")
    public ResponseEntity<PublicOrganizationInfo> getOrganizationInfoByAlias(@PathVariable String alias) {
        throw new NotImplementedException();
    }

    @PutMapping("/org/{id}/follow")
    public ResponseEntity<OperationResult> followOrganization(@PathVariable Long id) {
        throw new NotImplementedException();
    }

    @DeleteMapping("/org/{id}/follow")
    public ResponseEntity<OperationResult> unfollowOrganization(@PathVariable String id) {
        throw new NotImplementedException();
    }

    public record PublicEventInfo() {}

    public record PublicOrganizationInfo() {}


}
