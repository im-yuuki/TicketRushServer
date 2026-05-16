package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.PurchaseService;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.types.OperationResult;
import me.june8th.ticketrushserver.types.UpdateUserInfoPayload;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AccountService accountService;
    private final PurchaseService purchaseService;
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<FullUserInfo> getInfo(@AuthenticationPrincipal long id) {
        return ResponseEntity.ok(new FullUserInfo(storageService, accountService.getUserData(id)));
    }

    @PatchMapping
    public ResponseEntity<OperationResult> updateInfo(@AuthenticationPrincipal long id, @RequestBody UpdateUserInfoPayload payload) {
        accountService.updateUserInfo(id, payload);
        return ResponseEntity.ok(OperationResult.success("User information updated successfully"));
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

    @PutMapping("/follow/{id}")
    public ResponseEntity<OperationResult> followOrganization(@AuthenticationPrincipal long userId, @PathVariable Long id) {
        accountService.followOrganization(userId, id);
        return ResponseEntity.ok(OperationResult.success("Organization followed successfully"));
    }

    @DeleteMapping("/follow/{id}")
    public ResponseEntity<OperationResult> unfollowOrganization(@AuthenticationPrincipal long userId, @PathVariable Long id) {
        accountService.unfollowOrganization(userId, id);
        return ResponseEntity.ok(OperationResult.success("Organization unfollowed successfully"));
    }

    public record FullUserInfo(
            long id,
            String name,
            String email,
            String avatarUrl,
            Date birthDate,
            String gender,
            String phoneNumber,
            String addressLine,
            Instant createdAt,
            Instant updatedAt
    ) {
        public FullUserInfo(StorageService storageService, UserAccount user) {
            this(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    storageService.generatePresignedUrl(user.getAvatarKey()),
                    user.getBirthDate(),
                    user.getGender().toString(),
                    user.getPhoneNumber(),
                    user.getAddressLine(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        }
    }

}
