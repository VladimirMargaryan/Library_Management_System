package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Receipt;
import com.app.library.model.ReceiptStatus;
import com.app.library.repository.ReceiptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;

    public ReceiptServiceImpl(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    @Override
    public List<Receipt> getAll() {
        return receiptRepository.findAll();
    }

    @Override
    public List<Receipt> getAllByUserId(Long userId) {
        List<Receipt> receipts = receiptRepository.getAllByUserId(userId);
        if (receipts.isEmpty())
            log.info("Receipts not found by user id " + userId);
        return receipts;
    }

    @Override
    public List<Receipt> getAllByBookId(Long bookId) {
        List<Receipt> receipts = receiptRepository.getAllByBookId(bookId);
        if (receipts.isEmpty())
            log.info("Receipts not found by book id " + bookId);
        return receipts;
    }

    @Override
    public List<Receipt> getAllByOrderStatus(ReceiptStatus status) {
        List<Receipt> receipts = receiptRepository.getAllByReceiptStatus(status);
        if (receipts.isEmpty())
            log.info("Receipts not found by receipt status " + status.toString());
        return receipts;
    }

    @Override
    public List<Receipt> getAllByBookIdAndUserIdAndReceiptStatus(Long bookId, Long userId, ReceiptStatus status) {
        List<Receipt> receipts = receiptRepository.getAllByBookIdAndUserIdAndReceiptStatus(bookId, userId, status);
        if (receipts.isEmpty())
            log.info("Receipts not found by book id " + bookId + ", user id " + userId + ", receipt status " + status.toString());
        return receipts;
    }

    @Override
    public Receipt getByISBN(String ISBN) {
        Receipt receipt = receiptRepository.getByISBN(ISBN);
        if (receipt == null)
            log.info("Receipt not found by the ISBN " + ISBN);
        else
            log.info("Receipt founded by the ISBN " + ISBN);

        return receipt;
    }

    @Override
    public Receipt getById(Long id) throws NotFoundException {
        Optional<Receipt> order = receiptRepository.findById(id);
        if (!order.isPresent()) {
            log.error("Receipt not found by the id " + id);
            throw new NotFoundException("Receipt not found by the id " + id);
        }
        log.info("Receipt founded by the id " + id);
        return order.get();
    }

    @Transactional
    @Override
    public Receipt save(Receipt receipt) {
        Receipt saved = receiptRepository.save(receipt);
        log.info("Receipt saved! " + saved);
        return saved;
    }

    @Override
    public Receipt getReceiptByBookIdAndUserIdAndReceiptStatus(Long bookId, Long userId, ReceiptStatus status) {
        Receipt receipt = receiptRepository.getReceiptByBookIdAndUserIdAndReceiptStatus(bookId, userId, status);
        if (receipt == null)
            log.info("Receipt not found by book id " + bookId + ", user id " + userId + ", receipt status " + status.toString());
        return receipt;
    }

    @Transactional
    @Override
    public void removeById(Long id) throws NotFoundException {
        Optional<Receipt> optionalReceipt = receiptRepository.findById(id);
        if (!optionalReceipt.isPresent()){
            log.error("Receipt not found by the id " + id);
            throw new NotFoundException("Receipt not found by the id " + id);
        }
        receiptRepository.deleteById(id);
        log.info("Receipt removed!");
    }

    @Transactional
    @Override
    public Receipt update(Receipt receipt) throws NotFoundException {
        Receipt updatedReceipt = getById(receipt.getId());
        updatedReceipt.setOrderDate(receipt.getOrderDate());
        updatedReceipt.setReceiptStatus(receipt.getReceiptStatus());
        updatedReceipt.setBookId(receipt.getBookId());
        updatedReceipt.setExpirationDate(receipt.getExpirationDate());
        updatedReceipt.setUserId(receipt.getUserId());
        Receipt updated = save(updatedReceipt);
        log.info("Receipt updated! " + updated);
        return updated;
    }
}
