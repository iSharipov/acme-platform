package io.github.isharipov.acme.platform.external.repository;

import io.github.isharipov.acme.platform.external.model.ExternalProjectProjection;
import io.github.isharipov.acme.platform.external.model.UserExternalProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserExternalProjectRepository extends JpaRepository<UserExternalProject, UUID> {
    Page<ExternalProjectProjection> findAllByUserId(UUID userId, Pageable pageable);
}
