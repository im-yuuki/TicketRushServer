package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.types.OperationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization")
public class OrganizationController {

    @GetMapping("/info")
    public ResponseEntity<?> getOrganizationInfo() {
        throw new NotImplementedException();
    }

    @PatchMapping("/info")
    public ResponseEntity<?> updateOrganizationInfo() {
        throw new NotImplementedException();
    }

    @GetMapping("/events")
    public ResponseEntity<?> getOrganizationEvents() {
        throw new NotImplementedException();
    }

    @PostMapping("/events")
    public ResponseEntity<OperationResult> createEvent() {
        throw new NotImplementedException();
    }

}
