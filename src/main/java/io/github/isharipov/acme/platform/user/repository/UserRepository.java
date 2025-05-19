package io.github.isharipov.acme.platform.user.repository;

import io.github.isharipov.acme.platform.user.model.UserProfile;
import io.github.isharipov.acme.platform.user.model.UserProfileProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserProfile, Long> {

    boolean existsByAuthId(UUID authId);

    Optional<UserProfileProjection> findByAuthId(UUID authId);
}
