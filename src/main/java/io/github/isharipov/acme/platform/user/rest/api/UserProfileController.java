package io.github.isharipov.acme.platform.user.rest.api;

import io.github.isharipov.acme.platform.common.dto.Principal;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectOutboundDto;
import io.github.isharipov.acme.platform.project.external.service.UserExternalProjectService;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileInboundDto;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserExternalProjectService userExternalProjectService;

    public UserProfileController(UserProfileService userProfileService, UserExternalProjectService userExternalProjectService) {
        this.userProfileService = userProfileService;
        this.userExternalProjectService = userExternalProjectService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileOutboundDto> getUserProfile(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(userProfileService.getUserProfileByAuthId(principal));
    }

    @GetMapping("/{userId}/projects")
    public ResponseEntity<Page<ExternalProjectOutboundDto>> getUserProjects(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userExternalProjectService.getUserProjects(userId, pageable));
    }

    @GetMapping("/me/projects")
    public ResponseEntity<Page<ExternalProjectOutboundDto>> getCurrentUserProjects(
            @AuthenticationPrincipal Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size);
        var userProfileByAuthId = userProfileService.getUserProfileByAuthId(principal);
        return ResponseEntity.ok(userExternalProjectService.getUserProjects(userProfileByAuthId.id(), pageable));
    }

    @PostMapping("/me")
    public ResponseEntity<UserProfileOutboundDto> createOrRestoreProfile(@AuthenticationPrincipal Principal principal) {
        var profile = userProfileService.createOrRestoreUserProfile(principal.authId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileOutboundDto> updateProfileSelf(@AuthenticationPrincipal Principal principal, @Valid @RequestBody UserProfileInboundDto user) {
        var profile = userProfileService.updateUserProfile(principal, user);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteSelf(@AuthenticationPrincipal Principal principal) {
        userProfileService.softDeleteUserProfile(principal.authId());
        return ResponseEntity.noContent().build();
    }
}
