package com.app.library.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ListForm {

    private List<Book> books;
    private List<User> users;
    private List<ListEntry<Book, User>> bookUserEntry;
    private Set<Book> bookSet;

    public ListForm(List<Book> books) {
        this.books = books;
    }
}
