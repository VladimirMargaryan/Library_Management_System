package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Author;
import com.app.library.model.Gender;
import com.app.library.repository.AuthorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthorServiceImpl implements AuthorService{

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public Author getById(Long id) throws NotFoundException {
        Optional<Author> optionalAuthor = authorRepository.findById(id);
        if (!optionalAuthor.isPresent()) {
            log.error("Author not found by id: " + id + "!");
            throw new NotFoundException("Author not found by id: " + id + "!");
        }
        log.info("Author founded by id: " + id + "!");
        return optionalAuthor.get();
    }

    @Override
    public Author getByBookId(Long bookId) {
        Author author = authorRepository.getByBookId(bookId);
        if (author == null)
            log.error("Author not found by bookId: " + bookId + "!");
        else
            log.info("Author founded by bookId: " + bookId + "!");
        return author;
    }

    @Override
    public List<Author> getAll() {
        return authorRepository.findAll();
    }

    @Transactional
    @Override
    public Author save(Author author) {
        Author foundedAuthor = getByFirstnameAndLastnameAndGender(author.getFirstname(), author.getLastname(), author.getGender());
        if (foundedAuthor == null) {
            Author saved = authorRepository.save(author);
            log.info("New author saved " + saved);
            return saved;
        } else {
            log.info("Author founded by firstname and lastname and gender and not saved into database. " + foundedAuthor);
            return foundedAuthor;
        }
    }

    @Override
    public Author getByFirstnameAndLastnameAndGender(String firstname, String lastname, Gender gender) {
        return authorRepository.getAuthorByFirstnameAndLastnameAndGender(firstname, lastname, gender);
    }

    @Transactional
    @Override
    public void removeById(Long id) {
        try {
            Author author = getById(id);
        } catch (NotFoundException e) {
            log.error("There is no author by the id " + id + "!");
            log.error(e.getMessage());
            e.printStackTrace();
        }
        authorRepository.deleteById(id);
        log.info("author by id " + id + " successfully deleted!");
    }

    @Transactional
    @Override
    public Author update(Author author) throws NotFoundException {
        Author newAuthor = getById(author.getId());
        newAuthor.setFirstname(author.getFirstname());
        newAuthor.setLastname(author.getLastname());
        newAuthor.setBooks(author.getBooks());
        newAuthor.setGender(author.getGender());
        Author saved = save(newAuthor);
        log.info("Author updated! " + author);
        return saved;
    }
}
