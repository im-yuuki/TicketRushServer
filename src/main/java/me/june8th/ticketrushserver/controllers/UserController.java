package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.PurchaseService;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.types.OperationResult;
import me.june8th.ticketrushserver.types.PurchaseData;
import me.june8th.ticketrushserver.types.UpdateUserInfoPayload;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
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
    public ResponseEntity<OperationResult> updateAvatar(@AuthenticationPrincipal long id, @RequestPart("file") MultipartFile file) throws IOException {
        accountService.updateAvatar(id, file);
        return ResponseEntity.ok(OperationResult.success("Avatar updated successfully"));
    }

    @GetMapping("/tickets")
    public ResponseEntity<Collection<PurchaseData.UserTicketView>> getPurchasedTickets(@AuthenticationPrincipal long id) {
        return ResponseEntity.ok(purchaseService.getUserTickets(id));
    }

    @GetMapping("/follows")
    public ResponseEntity<Collection<OrganizationInfo>> getFollowedOrganizations(@AuthenticationPrincipal long id) {
        return ResponseEntity.ok(accountService.getFollowedOrganizations(id).stream()
                .map(org -> new OrganizationInfo(storageService, org))
                .toList());
    }

    @GetMapping("/follow/{id}")
    public ResponseEntity<IsFollowingResponse> checkIfFollowing(@AuthenticationPrincipal long userId, @PathVariable Long id) {
        boolean isFollowing = accountService.isFollowingOrganization(userId, id);
        return ResponseEntity.ok(new IsFollowingResponse(id, isFollowing));
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

    public record OrganizationInfo(
            long id,
            String name,
            String description,
            String aliasName,
            String avatarUrl,
            String bannerUrl,
            String websiteUrl,
            boolean verified
    ) {

        public OrganizationInfo(StorageService storageService, OrganizationAccount organization) {
            this(
                    organization.getId(),
                    organization.getName(),
                    organization.getDescription(),
                    organization.getAliasName(),
                    storageService.generatePresignedUrl(organization.getAvatarKey()),
                    storageService.generatePresignedUrl(organization.getBannerKey()),
                    organization.getWebsiteUrl(),
                    organization.getVerified()
            );
        }

    }

    public record IsFollowingResponse(long organizationId, boolean isFollowing) {

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
