package io.github.isharipov.acme.platform.project.external.model;

import io.github.isharipov.acme.platform.common.model.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Entity
@Table(name = "user_external_projects")
public class UserExternalProject extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "name")
    private String name;

    @Column(name = "user_id")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserExternalProject that = (UserExternalProject) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
