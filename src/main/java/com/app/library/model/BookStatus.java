package com.app.library.model;

public enum BookStatus {
    CHECKED_IN("Checked in"),
    CHECKED_OUT("Checked out"),
    NO_CIRCULATION("No circulation"),
    RESERVED("Reserved"),
    CHECKED_OUT_AND_RESERVED("Checked out and reserved"),
    READY_FOR_PICK_UP("Ready for pick up");

    private final String value;

    BookStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
