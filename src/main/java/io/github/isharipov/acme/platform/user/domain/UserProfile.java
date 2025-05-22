package io.github.isharipov.acme.platform.user.domain;

import io.github.isharipov.acme.platform.common.model.Auditable;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile extends Auditable {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "auth_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID authId;

    @Column(name = "name")
    private String name;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAuthId() {
        return authId;
    }

    public void setAuthId(UUID authId) {
        this.authId = authId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
