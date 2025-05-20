package io.github.isharipov.acme.platform.external.rest;

import io.github.isharipov.acme.platform.external.dto.ExternalProjectInboundDto;
import io.github.isharipov.acme.platform.external.dto.ExternalProjectOutboundDto;
import io.github.isharipov.acme.platform.external.dto.ExternalProjectUpdateInboundDto;
import io.github.isharipov.acme.platform.external.service.UserExternalProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/projects/external")
public class UserExternalProjectController {

    private final UserExternalProjectService userExternalProjectService;

    public UserExternalProjectController(UserExternalProjectService userExternalProjectService) {
        this.userExternalProjectService = userExternalProjectService;
    }

    @PostMapping
    public ResponseEntity<ExternalProjectOutboundDto> createExternalProject(@Valid @RequestBody ExternalProjectInboundDto externalProject) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userExternalProjectService.createExternalProject(externalProject));
    }

    @PutMapping("/{externalProjectId}")
    public ResponseEntity<ExternalProjectOutboundDto> updateExternalProject(@PathVariable UUID externalProjectId,
                                                                            @Valid @RequestBody ExternalProjectUpdateInboundDto externalProject) {
        return ResponseEntity.ok(userExternalProjectService.updateExternalProject(externalProjectId, externalProject));
    }
}
