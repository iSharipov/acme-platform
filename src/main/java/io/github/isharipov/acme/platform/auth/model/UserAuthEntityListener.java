package io.github.isharipov.acme.platform.auth.model;

import io.github.isharipov.acme.platform.common.util.BeanUtil;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserAuthEntityListener {

    @PreUpdate
    @PrePersist
    public void encodePassword(UserAuth user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
            PasswordEncoder encoder = BeanUtil.getBean(PasswordEncoder.class);
            user.setPassword(encoder.encode(user.getPassword()));
        }
    }
}