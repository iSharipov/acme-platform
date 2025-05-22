package io.github.isharipov.acme.platform.user.repository;

import io.github.isharipov.acme.platform.user.domain.UserProfile;
import io.github.isharipov.acme.platform.user.domain.UserProfileProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfileProjection> findByAuthIdAndDeletedFalse(UUID authId);

    Optional<UserProfile> findEntityByAuthIdAndDeletedFalse(UUID authId);

    Optional<UserProfileProjection> findByIdAndDeletedFalse(UUID id);

    Optional<UserProfile> findEntityByAuthId(UUID authId);
}
