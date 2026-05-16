package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.PurchaseService;
import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.types.OperationResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AccountService accountService;
    private final PurchaseService purchaseService;

    @GetMapping
    public ResponseEntity<?> getInfo(@AuthenticationPrincipal long id) {
        throw new NotImplementedException();
    }

    @PutMapping
    public ResponseEntity<OperationResult> updateInfo(@AuthenticationPrincipal long id) {
        throw new NotImplementedException();
    }

    @PutMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OperationResult> updateAvatar(@AuthenticationPrincipal long id, @RequestParam("file") MultipartFile file) throws IOException {
        accountService.updateAvatar(id, file);
        return ResponseEntity.ok(OperationResult.success("Avatar updated successfully"));
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getPurchasedTickets(@AuthenticationPrincipal long id) {
        return ResponseEntity.ok(purchaseService.getUserTickets(id));
    }

}
