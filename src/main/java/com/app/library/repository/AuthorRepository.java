package com.app.library.repository;

import com.app.library.model.Author;
import com.app.library.model.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query(value = "select * from author a where " +
            "a.id=(select author_id from book_authors where book_id=:bookId)", nativeQuery = true)
    Author getByBookId(@Param("bookId") Long id);

    Author getAuthorByFirstnameAndLastnameAndGender(String firstname, String lastname, Gender gender);
}
