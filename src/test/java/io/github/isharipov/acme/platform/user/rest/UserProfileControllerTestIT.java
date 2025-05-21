package io.github.isharipov.acme.platform.user.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.github.isharipov.acme.platform.auth.rest.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.RegisterInboundDto;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileInboundDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
class UserProfileControllerTestIT {

    private static final String PASSWORD = "StrongPassword123!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnUserProfile_whenAuthenticated() throws Exception {
        // GIVEN
        var email = new Faker().internet().emailAddress();
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

        // WHEN
        // THEN
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldReturnUnauthorized_whenGettingProfileWithoutToken() throws Exception {
        // GIVEN
        // WHEN
        // THEN
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnEmptyProjects_whenUserHasNone() throws Exception {
        // GIVEN
        var email = new Faker().internet().emailAddress();

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

        // WHEN
        var profileResult = mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var userId = UUID.fromString(objectMapper.readTree(profileResult.getResponse().getContentAsString())
                .at("/id").asText());

        // THEN
        mockMvc.perform(get("/api/users/" + userId + "/projects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldReturnNotFound_whenProfileWasSoftDeleted() throws Exception {
        // GIVEN
        var email = new Faker().internet().emailAddress();

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

        // WHEN
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // THEN
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User Profile not found"));
    }

    @Test
    void shouldCreateProfileAgain_afterSoftDelete() throws Exception {
        // GIVEN
        var email = new Faker().internet().emailAddress();

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

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        // WHEN
        mockMvc.perform(post("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());

        // THEN
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateProfile_whenValidRequest() throws Exception {
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

        var updateRequest = new UserProfileInboundDto("name");
        // THEN
        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"));
    }

    @Test
    void shouldFailUpdate_whenInvalidRequest() throws Exception {
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

        var invalidRequest = new UserProfileInboundDto("");
        // THEN
        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnEmptyProjectListForCurrentUser() throws Exception {
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
        mockMvc.perform(get("/api/users/me/projects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}