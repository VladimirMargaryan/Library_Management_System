package com.app.library.repository;

import com.app.library.model.Book;
import com.app.library.model.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("select b from Book b inner join b.authors c where concat(c.firstname, ' ', c.lastname, ' ',b.name, ' '," +
            " b.releaseYear, ' ', b.isbn, ' ', b.genre) like %?1%")
    Page<Book> findByAndSort(String keyword, Pageable pageable);
    List<Book> getAllByBookStatus(BookStatus bookStatus);
    Book getByIsbn(Long isbn);
    Book getBookByName(String name);
    List<Book> getAllByUsedBy(Long userId);
    Page<Book> getAllByUsedBy(Long userId, Pageable pageable);
    Page<Book> getAllByReservedBy(Long reservedBy, Pageable pageable);

    @Query("select b from Book b where b.usedBy = ?1 and b.returnDate < ?2")
    Page<Book> getBooksByUserIdAndWithExpirationDate(Long userId, Long currentTime, Pageable pageable);
    Page<Book> getBookByBookStatusAndReservedBy(BookStatus bookStatus, Long reservedBy, Pageable pageable);
    List<Book> getBookByBookStatusAndReservedBy(BookStatus bookStatus, Long reservedBy);

}
