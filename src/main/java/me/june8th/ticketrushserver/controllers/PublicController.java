package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.types.NotImplementedException;
import org.springframework.web.bind.annotation.*;

@RestController
public class PublicController {

    @GetMapping("/event/{id}")
    public String getEventInfo(@PathVariable String id) {
        throw new NotImplementedException();
    }

    @GetMapping("/org/{id}")
    public String getOrganizationInfo(@PathVariable String id) {
        throw new NotImplementedException();
    }

    @GetMapping("/org/@{alias}")
    public String getOrganizationInfoByAlias(@PathVariable String alias) {
        throw new NotImplementedException();
    }

}
