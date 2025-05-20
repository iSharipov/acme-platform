package io.github.isharipov.acme.platform.user.rest;

import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.external.dto.ExternalProjectOutboundDto;
import io.github.isharipov.acme.platform.external.service.UserExternalProjectService;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    public ResponseEntity<UserProfileOutboundDto> getUserProfile(@AuthenticationPrincipal UUID authId) {
        return ResponseEntity.ok(userProfileService.getUserProfileByAuthId(authId));
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
}
