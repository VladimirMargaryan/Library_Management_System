package com.app.library.model;

public enum BookGenre {
    CLASSICS("Classics"),
    FANTASY("Fantasy"),
    MYSTERY("Mystery"),
    THRILLER("Thriller"),
    ROMANCE("Romance"),
    WESTERNS("Westerns"),
    DYSTOPIAN("Dystopian"),
    CONTEMPORARY("Contemporary"),
    CONTEMPORARY_FICTION("Contemporary-fiction");

    private final String value;

    BookGenre(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
