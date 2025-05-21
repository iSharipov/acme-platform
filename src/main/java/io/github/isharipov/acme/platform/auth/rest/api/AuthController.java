package io.github.isharipov.acme.platform.auth.rest.api;

import io.github.isharipov.acme.platform.auth.rest.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.AuthOutboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.RefreshTokenInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.RegisterInboundDto;
import io.github.isharipov.acme.platform.auth.service.AuthService;
import io.github.isharipov.acme.platform.common.dto.Principal;
import io.github.isharipov.acme.platform.common.dto.TokenOutboundDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthOutboundDto> register(@Valid @RequestBody RegisterInboundDto registerRequest) {
        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/me")
                .build()
                .toUri();
        return ResponseEntity.created(location).body(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthOutboundDto> login(@Valid @RequestBody AuthInboundDto loginRequest) {
        return ResponseEntity.ok().body(authService.login(loginRequest));
    }

    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteSelf(@AuthenticationPrincipal Principal principal) {
        authService.deleteSelf(principal.authId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenOutboundDto> refreshToken(@Valid @RequestBody RefreshTokenInboundDto refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest.refreshToken()));
    }
}
