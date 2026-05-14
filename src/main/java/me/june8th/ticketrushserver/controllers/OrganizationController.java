package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.types.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
