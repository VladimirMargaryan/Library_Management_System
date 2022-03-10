package com.app.library.repository;

import com.app.library.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReceiptRepositoryTest {

    @Autowired
    private ReceiptRepository underTestReceiptRepository;

    @Autowired
    private BookRepository underTestBookRepository;

    @Autowired
    private AuthorRepository underTestAuthorRepository;

    @AfterEach
    void tearDown() {
        underTestBookRepository.deleteAll();
        underTestReceiptRepository.deleteAll();
        underTestAuthorRepository.deleteAll();
    }

    @Test
    void getByISBN() {

        // given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        long givenIsbn = 25465214L;

        Book givenBook1 = new Book();
        givenBook1.setName("book1");
        givenBook1.setGenre(BookGenre.CONTEMPORARY);
        givenBook1.setReleaseYear(1234);
        givenBook1.setIsbn(213456342L);
        givenBook1.setBookStatus(BookStatus.CHECKED_IN);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook1.setAuthors(authorList);

        Book givenBook2 = new Book();
        givenBook2.setName("book2");
        givenBook2.setGenre(BookGenre.CONTEMPORARY);
        givenBook2.setReleaseYear(567);
        givenBook2.setIsbn(givenIsbn);
        givenBook2.setBookStatus(BookStatus.CHECKED_IN);
        givenBook2.setAuthors(authorList);

        underTestBookRepository.save(givenBook1);
        underTestBookRepository.save(givenBook2);
        underTestAuthorRepository.save(givenAuthor);

        Receipt receipt = new Receipt(
                123433L,
                652534L,
                1L,
                2L,
                ReceiptStatus.ACTIVE);

        Receipt expected = underTestReceiptRepository.save(receipt);

        // when
        Receipt result = underTestReceiptRepository.getByISBN(String.valueOf(givenIsbn));

        // then
        assertThat(result).isEqualTo(expected);
    }
}