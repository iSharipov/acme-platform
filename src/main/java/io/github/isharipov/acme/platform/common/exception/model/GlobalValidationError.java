package io.github.isharipov.acme.platform.common.exception.model;

public class GlobalValidationError implements ApiError {
    private String objectName;
    private String description;

    public GlobalValidationError(String objectName, String description) {
        this.objectName = objectName;
        this.description = description;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
