package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.types.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping
    public ResponseEntity<?> getInfo() {
        throw new NotImplementedException();
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getPurchasedTickets() {
        throw new NotImplementedException();
    }

}
