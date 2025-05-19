package io.github.isharipov.acme.platform.auth.rest;

import io.github.isharipov.acme.platform.auth.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.dto.AuthOutboundDto;
import io.github.isharipov.acme.platform.auth.dto.RegisterInboundDto;
import io.github.isharipov.acme.platform.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
}
