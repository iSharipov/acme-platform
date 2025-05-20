package io.github.isharipov.acme.platform.project.external.repository;

import io.github.isharipov.acme.platform.project.external.model.ExternalProjectProjection;
import io.github.isharipov.acme.platform.project.external.model.UserExternalProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserExternalProjectRepository extends JpaRepository<UserExternalProject, UUID> {
    Page<ExternalProjectProjection> findAllByUserId(UUID userId, Pageable pageable);
}
