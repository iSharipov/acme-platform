package io.github.isharipov.acme.platform.auth.repository;

import io.github.isharipov.acme.platform.auth.domain.UserAuth;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.vault.enabled=false"
        }
)
@ActiveProfiles("test")
class UserAuthRepositoryTest {

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Test
    void shouldThrowConstraintViolation_whenSavingEntityWithEmptyEmail() {
        var user = new UserAuth();
        user.setEmail("");
        user.setPassword("StrongPassword123!");

        assertThrows(ConstraintViolationException.class, () -> {
            userAuthRepository.saveAndFlush(user);
        });
    }
}