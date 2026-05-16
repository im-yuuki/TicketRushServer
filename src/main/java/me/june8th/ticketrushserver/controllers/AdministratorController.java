package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.types.OperationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdministratorController {

    private final AccountService accountService;

    @PutMapping("/organizations")
    public ResponseEntity<OperationResult> createOrganization(@RequestBody CreateOrganizationPayload payload) {
        OrganizationAccount organization = accountService.createOrganizationAccount(
                payload.name(), payload.email(), payload.password()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OperationResult.success("Organization created successfully", organization.getId()));
    }

    @PostMapping("/organization/{id}/verify")
    public ResponseEntity<OperationResult> verifyOrganization(@PathVariable Long id) {
        accountService.verifyOrganizationAccount(id);
        return ResponseEntity.ok(OperationResult.success("Organization verified successfully"));
    }

    @PostMapping("/account/{id}/lock")
    public ResponseEntity<OperationResult> lockAccount(@PathVariable Long id) {
        accountService.lockAccount(id);
        return ResponseEntity.ok(OperationResult.success("Account locked successfully"));
    }

    @PostMapping("/account/{id}/unlock")
    public ResponseEntity<OperationResult> unlockAccount(@PathVariable Long id) {
        accountService.unlockAccount(id);
        return ResponseEntity.ok(OperationResult.success("Account unlocked successfully"));
    }

    public record CreateOrganizationPayload(
            String name,
            String email,
            String password
    ) {}


}
