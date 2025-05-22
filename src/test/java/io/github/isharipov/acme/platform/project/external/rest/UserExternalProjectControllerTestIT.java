package io.github.isharipov.acme.platform.project.external.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.github.isharipov.acme.platform.auth.rest.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.RegisterInboundDto;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectInboundDto;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectUpdateInboundDto;
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
class UserExternalProjectControllerTestIT {

    private static final String PASSWORD = "StrongPassword123!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateExternalProject_whenAuthenticated() throws Exception {
        // GIVEN
        var faker = new Faker();
        var email = faker.internet().emailAddress();
        register(email);
        var accessToken = loginAndGetToken(email);
        var userId = getUserId(accessToken);

        var inboundDto = new ExternalProjectInboundDto(
                faker.internet().uuid(),
                "JIRA-123",
                userId
        );

        // WHEN
        // THEN
        mockMvc.perform(post("/api/projects/external")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inboundDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("JIRA-123"))
                .andExpect(jsonPath("$.externalId").value(inboundDto.externalId()));
    }

    @Test
    void shouldUpdateExternalProject_whenAuthenticated() throws Exception {
        // GIVEN
        var faker = new Faker();
        var email = faker.internet().emailAddress();
        register(email);
        var accessToken = loginAndGetToken(email);
        var userId = getUserId(accessToken);

        var createDto = new ExternalProjectInboundDto(
                faker.internet().uuid(),
                "JIRA-INIT",
                userId
        );

        var createdResponse = mockMvc.perform(post("/api/projects/external")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        var createdId = UUID.fromString(
                objectMapper.readTree(createdResponse.getResponse().getContentAsString()).get("id").asText()
        );

        var updateDto = new ExternalProjectUpdateInboundDto("JIRA-UPDATED", userId);

        // WHEN
        // THEN
        mockMvc.perform(
                        put("/api/projects/external/" + createdId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId.toString()))
                .andExpect(jsonPath("$.name").value("JIRA-UPDATED"))
                .andExpect(jsonPath("$.externalId").value(createDto.externalId()));
    }

    @Test
    void shouldReturnBadRequest_whenJsonIsMalformedOrInvalid() throws Exception {
        // GIVEN
        var email = new Faker().internet().emailAddress();
        register(email);
        var accessToken = loginAndGetToken(email);

        // WHEN
        var invalidJson = """
                {
                  "externalId": 12345,
                  "name": "Bad input",
                  "userId": "not-a-uuid"
                }
                """;
        // THEN
        mockMvc.perform(post("/api/projects/external")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Http Message Not Readable"))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void shouldReturnUnprocessableEntity_whenExternalIdIsArrayInsteadOfString() throws Exception {
        // GIVEN
        var faker = new Faker();
        var email = faker.internet().emailAddress();
        var password = "StrongPassword123!";

        var registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, password))))
                .andExpect(status().isCreated())
                .andReturn();

        var accessToken = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .at("/token/accessToken").asText();

        var userProfileResult = mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var userId = objectMapper.readTree(userProfileResult.getResponse().getContentAsString())
                .get("id").asText();

        var invalidPayload = """
                {
                  "externalId": [],
                  "name": "JIRA-123",
                  "userId": "%s"
                }
                """.formatted(userId);

        // WHEN
        // THEN
        mockMvc.perform(post("/api/projects/external")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Http Message Not Readable"))
                .andExpect(jsonPath("$.description").value("Unable to map request JSON: externalId"));
    }

    @Test
    void shouldReturnConflict_whenExternalProjectAlreadyExists() throws Exception {
        // GIVEN
        var faker = new Faker();
        var email = faker.internet().emailAddress();
        register(email);
        var accessToken = loginAndGetToken(email);
        var userId = getUserId(accessToken);

        var externalId = faker.internet().uuid();
        var inboundDto = new ExternalProjectInboundDto(
                externalId,
                "JIRA-DUPLICATE",
                userId
        );

        // Create first project
        mockMvc.perform(post("/api/projects/external")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inboundDto)))
                .andExpect(status().isCreated());

        // WHEN & THEN: Create second project with same externalId -> should return 409
        mockMvc.perform(post("/api/projects/external")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inboundDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("External project is already exists"));
    }


    private void register(String email) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterInboundDto(email, PASSWORD))))
                .andExpect(status().isCreated());
    }

    private String loginAndGetToken(String email) throws Exception {
        var login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthInboundDto(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(login.getResponse().getContentAsString())
                .at("/token/accessToken").asText();
    }

    private UUID getUserId(String token) throws Exception {
        var profile = mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(profile.getResponse().getContentAsString()).get("id").asText());
    }
}