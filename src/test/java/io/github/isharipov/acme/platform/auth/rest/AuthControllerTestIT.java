package io.github.isharipov.acme.platform.auth.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.github.isharipov.acme.platform.auth.rest.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.RefreshTokenInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.RegisterInboundDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.vault.enabled=false"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTestIT {

    private static final String PASSWORD = "StrongPassword123!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterNewUser_whenValidInputProvided() throws Exception {
        // GIVEN
        var faker = new Faker();
        var registerRequest = new RegisterInboundDto(faker.internet().emailAddress(), PASSWORD);

        // WHEN
        // THEN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token.accessToken").exists())
                .andExpect(jsonPath("$.token.refreshToken").exists());
    }

    @Test
    void shouldReturnTokens_whenValidCredentialsProvided() throws Exception {
        // GIVEN
        var faker = new Faker();
        var email = faker.internet().emailAddress();
        var registerRequest = new RegisterInboundDto(email, PASSWORD);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        var loginRequest = new AuthInboundDto(email, PASSWORD);

        // WHEN
        // THEN
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token.accessToken").exists())
                .andExpect(jsonPath("$.token.refreshToken").exists());
    }

    @Test
    void shouldReturnUnauthorized_whenInvalidPasswordProvided() throws Exception {
        // GIVEN
        var faker = new Faker();
        var emailAddress = faker.internet().emailAddress();
        var registerRequest = new RegisterInboundDto(emailAddress, PASSWORD);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // WHEN
        var loginRequest = new AuthInboundDto(emailAddress, "WrongPassword123");

        // THEN
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRefreshToken_whenValidRefreshTokenProvided() throws Exception {
        // GIVEN
        var faker = new Faker();
        var email = faker.internet().emailAddress();

        // WHEN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isCreated());

        var loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthInboundDto(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        var refreshToken = objectMapper
                .readTree(loginResult.getResponse().getContentAsString())
                .at("/token/refreshToken").asText();

        // THEN
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldReturnUnauthorized_whenRefreshTokenDoesNotMatch() throws Exception {
        // GIVEN
        var email = new Faker().internet().emailAddress();

        // WHEN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isCreated());

        var loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthInboundDto(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        var refreshTokenA = objectMapper
                .readTree(loginResult.getResponse().getContentAsString())
                .at("/token/refreshToken").asText();

        var secondLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthInboundDto(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        var refreshTokenB = objectMapper
                .readTree(secondLoginResult.getResponse().getContentAsString())
                .at("/token/refreshToken").asText();

        var request = new RefreshTokenInboundDto(refreshTokenA);

        // THEN
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnConflict_whenRegisteringExistingEmail() throws Exception {
        // GIVEN
        var faker = new Faker();
        var email = faker.internet().emailAddress();

        // WHEN
        var registerRequest = new RegisterInboundDto(email, PASSWORD);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        // THEN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        // GIVEN
        var registerRequest = new RegisterInboundDto("invalid-email", PASSWORD);
        // WHEN
        // THEN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenLoginInputIsInvalid() throws Exception {
        // GIVEN
        var loginRequest = new AuthInboundDto("", PASSWORD);
        // WHEN
        // THEN
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorized_whenRefreshTokenIsInvalid() throws Exception {
        // GIVEN
        var refreshRequest = new RefreshTokenInboundDto("invalid-token-value");
        // WHEN
        // THEN
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDeleteUser_whenAuthenticated() throws Exception {
        // GIVEN
        var email = new Faker().internet().emailAddress();
        // WHEN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isCreated());

        var loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthInboundDto(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        var accessToken = objectMapper
                .readTree(loginResult.getResponse().getContentAsString())
                .at("/token/accessToken").asText();

        // THEN
        mockMvc.perform(delete("/auth/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnUnauthorized_whenDeletingUserWithoutToken() throws Exception {
        // GIVEN
        // WHEN
        // THEN
        mockMvc.perform(delete("/auth/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRestoreUser_whenReRegisteringDeletedUser() throws Exception {
        // GIVEN
        var email = new Faker().internet().emailAddress();
        // WHEN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isCreated());

        var loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthInboundDto(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        var accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .at("/token/accessToken").asText();

        // THEN
        mockMvc.perform(delete("/auth/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBadRequest_whenRefreshTokenIsMissing() throws Exception {
        // GIVEN
        // WHEN
        // THEN
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorized_whenDeletedUserTriesToLogin() throws Exception {
        // GIVEN
        var email = new Faker().internet().emailAddress();
        // WHEN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isCreated());

        var loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthInboundDto(email, PASSWORD))))
                .andReturn();

        var accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .at("/token/accessToken").asText();
        // THEN
        mockMvc.perform(delete("/auth/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthInboundDto(email, PASSWORD))))
                .andExpect(status().isUnauthorized());
    }
}