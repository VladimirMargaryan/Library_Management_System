package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Author;
import com.app.library.model.Gender;

import java.util.List;

public interface AuthorService {

    Author getById(Long id) throws NotFoundException;
    Author getByBookId(Long book_id);
    List<Author> getAll();
    Author save(Author author);
    Author getByFirstnameAndLastnameAndGender(String firstname, String lastname, Gender gender);
    void removeById(Long id);
    Author update(Author author) throws NotFoundException;
}
