package io.github.isharipov.acme.platform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterInboundDto(@NotBlank
                                 @Email(message = "Login must be a valid email")
                                 String email,
                                 @NotBlank
                                 @Size(min = 8, max = 255, message = "Password must be between 8 and 129 characters")
                                 @Pattern(
                                         regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                                         message = "Password must contain at least one letter and one number"
                                 )
                                 String password) {
}
