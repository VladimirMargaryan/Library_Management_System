package com.app.library.repository;

import com.app.library.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private AuthorRepository underTestAuthorRepository;

    @Autowired
    private BookRepository underTestBookRepository;


    @AfterEach
    void tearDown() {
        underTestAuthorRepository.deleteAll();
        underTestBookRepository.deleteAll();
    }


    @Test
    void itShouldFindBookByKeywordAndSort() {

        // given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        String givenKeyword = "book";

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
        givenBook2.setIsbn(25465214L);
        givenBook2.setBookStatus(BookStatus.CHECKED_IN);
        givenBook2.setAuthors(authorList);

        Book saved1 = underTestBookRepository.save(givenBook1);
        Book saved2 = underTestBookRepository.save(givenBook2);
        underTestAuthorRepository.save(givenAuthor);

        PageRequest pageable = PageRequest.of(0,5, Sort.by("id"));
        List<Book> list = new ArrayList<>();
        list.add(saved1);
        list.add(saved2);
        Page<Book> bookPage = new PageImpl<>(list, pageable, 5);

        // when
        Page<Book> byAndSort = underTestBookRepository.findByAndSort(givenKeyword, pageable);

        // then
        assertThat(byAndSort.getContent()).isEqualTo(bookPage.getContent());
    }


    @Test
    void getBooksByUserIdAndWithExpirationDate() {

        // given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        String givenKeyword = "book";
        long usedBy = 1L;
        long currentTime = 12345L;

        Book givenBook1 = new Book();
        givenBook1.setName("book1");
        givenBook1.setGenre(BookGenre.CONTEMPORARY);
        givenBook1.setReleaseYear(1234);
        givenBook1.setIsbn(213456342L);
        givenBook1.setBookStatus(BookStatus.CHECKED_IN);
        givenBook1.setUsedBy(usedBy);
        givenBook1.setReturnDate(1234L);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook1.setAuthors(authorList);

        Book givenBook2 = new Book();
        givenBook2.setName("book2");
        givenBook2.setGenre(BookGenre.CONTEMPORARY);
        givenBook2.setReleaseYear(567);
        givenBook2.setIsbn(25465214L);
        givenBook2.setBookStatus(BookStatus.CHECKED_IN);
        givenBook2.setUsedBy(usedBy);
        givenBook2.setReturnDate(1234L);
        givenBook2.setAuthors(authorList);

        Book saved1 = underTestBookRepository.save(givenBook1);
        Book saved2 = underTestBookRepository.save(givenBook2);
        underTestAuthorRepository.save(givenAuthor);

        PageRequest pageable = PageRequest.of(0,5, Sort.by("id"));
        List<Book> list = new ArrayList<>();
        list.add(saved1);
        list.add(saved2);
        Page<Book> bookPage = new PageImpl<>(list, pageable, 5);


        // when
        Page<Book> booksByUserIdAndWithExpirationDate =
                underTestBookRepository.getBooksByUserIdAndWithExpirationDate(usedBy, currentTime, pageable);

        // Then
        assertThat(booksByUserIdAndWithExpirationDate.getContent()).isEqualTo(bookPage.getContent());

    }

}