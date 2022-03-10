package com.app.library.repository;

import com.app.library.model.*;
import com.app.library.model.Author;
import com.app.library.model.Gender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorRepository underTestAuthorRepository;

    @Autowired
    private BookRepository underTestBookRepository;

    @AfterEach
    void tearDown() {
        underTestAuthorRepository.deleteAll();
        underTestBookRepository.deleteAll();
    }


    @BeforeEach
    void setUp() {

    }

    @Test
    void itShouldGetAuthorByBookId() {

        //given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);
        givenBook.setIsbn(213456342L);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);

        Book savedBook = underTestBookRepository.save(givenBook);

        Author savedAuthor = underTestAuthorRepository.save(givenAuthor);

        //when
        Author authorByBookId = underTestAuthorRepository.getByBookId(savedBook.getId());

        //then
        assertThat(authorByBookId).isEqualTo(savedAuthor);
    }

    @Test
    void itShouldNotGetAuthorByBookId() {

        //given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);
        givenBook.setIsbn(213456342L);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        givenBook.setAuthors(new ArrayList<>());

        Book savedBook = underTestBookRepository.save(givenBook);

        Author savedAuthor = underTestAuthorRepository.save(givenAuthor);

        //when
        Author authorByBookId = underTestAuthorRepository.getByBookId(savedBook.getId());

        //then
        assertThat(authorByBookId).isNotEqualTo(savedAuthor);

    }
}