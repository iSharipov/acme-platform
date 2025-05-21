package io.github.isharipov.acme.platform.common.util;

import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectInboundDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilTest {

    @Test
    void toJson_shouldReturnValidJson_whenGivenExternalProjectInboundDto() {
        // GIVEN
        var userId = UUID.randomUUID();
        var dto = new ExternalProjectInboundDto("jira-123", "name", userId);
        // WHEN
        var json = JsonUtil.toJson(dto);
        // THEN
        assertTrue(json.contains("\"externalId\":\"jira-123\""));
        assertTrue(json.contains("\"userId\":\"" + userId + "\""));
    }

    @Test
    void toJson_shouldThrowRuntimeException_whenJacksonFails() {
        // GIVEN
        var badObject = new Object() {
            private final Object self = this;

            public Object getSelf() {
                return self;
            }
        };
        // WHEN
        // THEN
        var ex = assertThrows(RuntimeException.class, () -> JsonUtil.toJson(badObject));
        assertTrue(ex.getMessage().contains("Failed to serialize object to JSON"));
    }
}
