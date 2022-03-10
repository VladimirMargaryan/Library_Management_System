package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.*;
import com.app.library.repository.AuthorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;
    private AuthorServiceImpl underTestAuthorService;


    @BeforeEach
    void setUp() {
        underTestAuthorService = new AuthorServiceImpl(authorRepository);
    }

    @AfterEach
    void tearDown() {
        authorRepository.deleteAll();
    }

    @Test
    void CanGetById() throws NotFoundException {

        //given
        long authorId = 1L;
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );
        givenAuthor.setId(authorId);

        doReturn(Optional.of(givenAuthor)).when(authorRepository).findById(authorId);

        //when
        Author result = underTestAuthorService.getById(authorId);

        //then
        assertThat(givenAuthor).isEqualTo(result);

    }

    @Test
    void CannotGetById() {

        //given
        long authorId = 1L;
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );
        givenAuthor.setId(authorId);

        //then
        assertThatThrownBy(() -> underTestAuthorService.getById(authorId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Author not found by id: " + authorId + "!");

    }


    @Test
    void CenGetByBookId() {

        //given
        long givenId = 1L;

        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        givenAuthor.setId(givenId);

        //when
        when(authorRepository.getByBookId(givenId)).thenReturn(givenAuthor);
        Author returnedAuthor = underTestAuthorService.getByBookId(givenId);


//        //then
        assertThat(givenAuthor).isEqualTo(returnedAuthor);

    }

    @Test
    void CenNotGetByBookId() {

        //given
        long givenId = 1L;

        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        givenAuthor.setId(givenId);

        //when
        when(authorRepository.getByBookId(givenId)).thenReturn(null);
        Author returnedAuthor = underTestAuthorService.getByBookId(givenId);


//        //then
        assertThat(givenAuthor).isNotEqualTo(returnedAuthor);

    }


    @Test
    void CenGetAll() {

        //when
        underTestAuthorService.getAll();

        //then
        verify(authorRepository).findAll();
    }

    @Test
    void CanSave() {

        //given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        //when
        underTestAuthorService.save(givenAuthor);

        //then
        ArgumentCaptor<Author> authorArgumentCaptor =
                ArgumentCaptor.forClass(Author.class);

        verify(authorRepository)
                .save(authorArgumentCaptor.capture());

        Author capturedAuthor = authorArgumentCaptor.getValue();

        assertThat(capturedAuthor).isEqualTo(givenAuthor);
    }

    @Test
    void notSavesBecauseOfDuplicate() {

        //given
        Author givenAuthor1 = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Author givenAuthor2 = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        given(authorRepository.getAuthorByFirstnameAndLastnameAndGender(
                givenAuthor2.getFirstname(),
                givenAuthor2.getLastname(),
                givenAuthor2.getGender()
        )).willReturn(givenAuthor1);

        //when
        underTestAuthorService.save(givenAuthor1);

    }

    @Test
    void CenGetByFirstnameAndLastnameAndGender() {
        // given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );


        //when
        underTestAuthorService.getByFirstnameAndLastnameAndGender(
                givenAuthor.getFirstname(),
                givenAuthor.getLastname(),
                givenAuthor.getGender()
        );

        //then
        verify(authorRepository).getAuthorByFirstnameAndLastnameAndGender(
                givenAuthor.getFirstname(),
                givenAuthor.getLastname(),
                givenAuthor.getGender()
        );
    }

    @Test
    void CanRemoveById() throws NotFoundException {

        //given
        long authorId = 1L;
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );
        givenAuthor.setId(authorId);

        doReturn(Optional.of(givenAuthor)).when(authorRepository).findById(authorId);

        //when
        underTestAuthorService.removeById(authorId);

        //then
        verify(authorRepository).deleteById(authorId);
    }

    @Test
    void CanNotRemoveById() throws NotFoundException {

        //given
        long authorId = 1L;

        //then
        assertThatThrownBy(() -> underTestAuthorService.removeById(authorId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("There is no author by the id " + authorId + "!");
    }


    @Test
    void CanUpdate() throws NotFoundException {
        // Given
        long authorId = 1L;
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );
        givenAuthor.setId(authorId);

        when(authorRepository.findById(givenAuthor.getId()))
                .thenReturn(Optional.of(givenAuthor));

        ArgumentCaptor<Author> argumentCaptor =
                ArgumentCaptor.forClass(Author.class);

        when(authorRepository.save(argumentCaptor.capture()))
                .thenAnswer(iom -> iom.getArgument(0));


        // When
        givenAuthor.setFirstname("FakeName");
        Author updated = underTestAuthorService.update(givenAuthor);


        // Then
        assertThat(argumentCaptor.getValue().getFirstname())
                .isEqualTo(updated.getFirstname());
    }

    @Test
    void CanNotUpdate() {
        //given
        long authorId = 1L;
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );
        givenAuthor.setId(authorId);

        //then
        assertThatThrownBy(() -> underTestAuthorService.update(givenAuthor))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Author not found by id: " + authorId + "!");

    }

}