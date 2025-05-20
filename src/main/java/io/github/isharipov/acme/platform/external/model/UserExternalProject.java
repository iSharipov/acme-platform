package io.github.isharipov.acme.platform.external.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_external_projects")
public class UserExternalProject {

    @Id
    @GeneratedValue(generator = "assigned-identity")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String externalId;

    @Column
    private String name;

    @Column
    private UUID userId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
