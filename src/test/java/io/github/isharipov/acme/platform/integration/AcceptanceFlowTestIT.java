package io.github.isharipov.acme.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.github.isharipov.acme.platform.auth.rest.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.RefreshTokenInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.RegisterInboundDto;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectInboundDto;
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
class AcceptanceFlowTestIT {

    private static final String PASSWORD = "StrongPassword123!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCoverFullEndToEndFlow() throws Exception {
        var faker = new Faker();
        var email = faker.internet().emailAddress();
        var externalId = faker.internet().uuid();

        // REGISTER USER
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isCreated());

        // REGISTER SAME USER AGAIN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isConflict());

        // LOGIN
        var loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthInboundDto(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        var loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        var accessToken = loginJson.at("/token/accessToken").asText();
        var refreshToken = loginJson.at("/token/refreshToken").asText();

        // GET PROFILE
        var profileResult = mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var userId = UUID.fromString(objectMapper.readTree(profileResult.getResponse().getContentAsString())
                .get("id").asText());

        // ADD EXTERNAL PROJECT
        var externalProject = new ExternalProjectInboundDto(externalId, "TEST_PROJECT", userId);
        mockMvc.perform(post("/api/projects/external")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(externalProject)))
                .andExpect(status().isCreated());

        // RETRIEVE EXTERNAL PROJECT
        mockMvc.perform(get("/api/users/me/projects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("TEST_PROJECT"));

        // RETRIEVE EXTERNAL PROJECT BY USER ID
        mockMvc.perform(get("/api/users/" + userId + "/projects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("TEST_PROJECT"));

        // REFRESH TOKEN
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenInboundDto(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // UNAUTHORIZED ACCESS CHECK
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());

        // BAD INPUT CHECK
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"not_valid_email\", \"password\": \"" + PASSWORD + "\"}"))
                .andExpect(status().isBadRequest());

        // DELETE USER
        mockMvc.perform(delete("/auth/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // REGISTER SAME USER AGAIN
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isCreated());
    }
}