package io.github.isharipov.acme.platform.auth.service.impl;

import io.github.isharipov.acme.platform.auth.domain.UserAuth;
import io.github.isharipov.acme.platform.auth.repository.UserAuthRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;

    public AuthUserDetailsService(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var userAuth = userAuthRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapToUserDetails(userAuth);
    }

    private UserDetails mapToUserDetails(UserAuth user) {
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .accountLocked(user.getStatus() == UserAuth.UserStatus.LOCKED)
                .disabled(user.getStatus() != UserAuth.UserStatus.ACTIVE)
                .build();
    }

}