package com.app.library.repository;

import com.app.library.model.Receipt;
import com.app.library.model.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    List<Receipt> getAllByUserId(Long userId);
    List<Receipt> getAllByBookId(Long bookId);
    List<Receipt> getAllByBookIdAndUserIdAndReceiptStatus(Long bookId, Long userId, ReceiptStatus status);
    List<Receipt> getAllByReceiptStatus(ReceiptStatus receiptStatus);
    Receipt getReceiptByBookIdAndUserIdAndReceiptStatus(Long bookId, Long userId, ReceiptStatus status);
    @Query(value = "select * from receipt o where book_id=(select b.id from book b where b.isbn=:isbn)", nativeQuery = true)
    Receipt getByISBN(@Param("isbn") String isbn);
}
