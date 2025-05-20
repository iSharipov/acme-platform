package io.github.isharipov.acme.platform.common.exception;

public class FieldValidationException extends Exception {
    private final String fieldName;
    private final String fieldMessage;

    public FieldValidationException(String fieldName, String fieldMessage) {
        this.fieldName = fieldName;
        this.fieldMessage = fieldMessage;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldMessage() {
        return fieldMessage;
    }
}
