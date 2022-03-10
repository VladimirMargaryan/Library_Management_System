package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Receipt;
import com.app.library.model.ReceiptStatus;

import java.util.List;

public interface ReceiptService {

    List<Receipt> getAll();
    List<Receipt> getAllByUserId(Long userId);
    List<Receipt> getAllByBookId(Long bookId);
    List<Receipt> getAllByOrderStatus(ReceiptStatus status);
    List<Receipt> getAllByBookIdAndUserIdAndReceiptStatus(Long bookId, Long userId, ReceiptStatus status);
    Receipt getByISBN(String ISBN);
    Receipt getById(Long id) throws NotFoundException;
    Receipt save(Receipt receipt);
    Receipt getReceiptByBookIdAndUserIdAndReceiptStatus(Long bookId, Long userId, ReceiptStatus status);
    void removeById(Long id) throws NotFoundException;
    Receipt update(Receipt receipt) throws NotFoundException;

}
