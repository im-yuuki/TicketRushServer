package me.june8th.ticketrushserver.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicController {

    @GetMapping("/status")
    public void getServerStatus() {
    }

    @GetMapping("/search")
    public void globalSearch() {
    }

}
