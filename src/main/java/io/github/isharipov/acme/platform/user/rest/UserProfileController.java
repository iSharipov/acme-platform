package io.github.isharipov.acme.platform.user.rest;

import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileOutboundDto> getUserProfile(@AuthenticationPrincipal UUID authId) {
        return ResponseEntity.ok(userProfileService.getUserProfileByAuthId(authId));
    }
}
