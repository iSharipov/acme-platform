package io.github.isharipov.acme.platform.common.exception.model;

public class FieldValidationError implements ApiError {
    private String fieldName;
    private String description;

    public FieldValidationError(String fieldName, String description) {
        this.fieldName = fieldName;
        this.description = description;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
