package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BookService {

    List<Book> getAll();
    Page<Book> getAll(Pageable pageable);
    Page<Book> search(String keyword, Pageable pageable);
    List<Book> getAllByStatus(BookStatus status);
    Book save(Book book);
    Book save(Book book, Author author);
    Book getById(Long id) throws NotFoundException;
    void removeById(Long id);
    Book update(Book book) throws NotFoundException;
    List<Book> getAllByUsedBy(Long id) throws NotFoundException;
    Page<Book> getAllByUsedBy(Long id, Pageable pageable);
    Page<Book> getAllByReservedBy(Long id, Pageable pageable);
    Page<Book> getAllByBookStatusAndReservedBy(BookStatus bookStatus, Long reservedBy, Pageable pageable);
    Page<Book> getBooksByUserIdAndWithExpirationDate(Long userId, Long currentTime, Pageable pageable);
    List<Book> getAllByBookStatusAndReservedBy(BookStatus bookStatus, Long reservedBy);
    void doReservation(Book book, User user) throws NotFoundException;
    void notifyingForPickingBookUp(Book book, User user) throws NotFoundException;
    void sendReservationReadyEmail(Book book, User user);
    void sendPickUpAndReceiptEmail(Long userId, List<Receipt> receipts) throws NotFoundException;
    void doPickUp(Long userId) throws NotFoundException;
    void doReturnBooks(Set<Book> books, Long userId) throws NotFoundException;
    void sendEmailAboutReturnedBook(Long userId, List<Receipt> receipts) throws NotFoundException;
    Page<Book> findPaginated(Pageable pageable, Collection<Book> books);
    Page<ListEntry<Book, User>> findPaginated(Pageable pageable, Map<Book, User> bookUserMap);
}
