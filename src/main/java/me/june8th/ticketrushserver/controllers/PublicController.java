package me.june8th.ticketrushserver.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/status")
    public void getServerStatus() {
        // TODO: Implement a more comprehensive status check (e.g., database connectivity, service health)
    }

    @GetMapping("/search")
    public void globalSearch() {
    }

}
