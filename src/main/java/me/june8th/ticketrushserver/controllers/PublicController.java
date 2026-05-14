package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.types.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PublicController {

    @GetMapping("/event/{id}")
    public ResponseEntity<PublicEventInfo> getEventInfo(@PathVariable String id) {
        throw new NotImplementedException();
    }

    @GetMapping("/org/{id}")
    public ResponseEntity<PublicOrganizationInfo> getOrganizationInfo(@PathVariable String id) {
        throw new NotImplementedException();
    }

    @GetMapping("/org/@{alias}")
    public ResponseEntity<PublicOrganizationInfo> getOrganizationInfoByAlias(@PathVariable String alias) {
        throw new NotImplementedException();
    }

    public record PublicEventInfo() {}

    public record PublicOrganizationInfo() {}


}
