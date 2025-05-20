package io.github.isharipov.acme.platform.common.exception.model;

public abstract class StandardResponse {
    private final boolean success;

    protected StandardResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
