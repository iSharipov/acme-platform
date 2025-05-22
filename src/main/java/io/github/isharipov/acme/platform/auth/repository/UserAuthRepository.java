package io.github.isharipov.acme.platform.auth.repository;

import io.github.isharipov.acme.platform.auth.domain.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, UUID> {

    boolean existsByEmail(String email);

    Optional<UserAuth> findByEmail(String email);
}
