package me.june8th.ticketrushserver.controllers;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RolesAllowed("ROLE_ADMINISTRATOR")
@RequiredArgsConstructor
public class AdministratorController {

}
