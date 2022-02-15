package com.app.library.model;

public enum UserStatus {
    VERIFIED("Verified"),
    UNVERIFIED("Unverified"),
    BLOCKED("Blocked");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
