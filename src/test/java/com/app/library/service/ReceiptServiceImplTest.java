package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Book;
import com.app.library.model.BookGenre;
import com.app.library.model.Receipt;
import com.app.library.model.ReceiptStatus;
import com.app.library.repository.ReceiptRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceImplTest {

    @Mock
    private ReceiptRepository receiptRepository;
    private ReceiptServiceImpl underTestReceiptService;

    @BeforeEach
    void setUp() {
        underTestReceiptService = new ReceiptServiceImpl(receiptRepository);
    }

    @AfterEach
    void tearDown() {
        receiptRepository.deleteAll();
    }

    @Test
    void getAll() {
        // when
        underTestReceiptService.getAll();

        // then
        verify(receiptRepository).findAll();
    }

    @Test
    void getAllByUserId() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        List<Receipt> receipts = new ArrayList<>();
        receipts.add(receipt);

        // when
        given(receiptRepository.getAllByUserId(receipt.getUserId()))
                .willReturn(receipts);

        assertThat(underTestReceiptService.getAllByUserId(receipt.getUserId()))
                .isEqualTo(receipts);
    }

    @Test
    void getAllByUserIdInCaseOfEmptyList() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        // when
        given(receiptRepository.getAllByUserId(receipt.getUserId()))
                .willReturn(Collections.emptyList());

        assertThat(underTestReceiptService.getAllByUserId(receipt.getUserId()))
                .isEqualTo(Collections.emptyList());
    }


    @Test
    void getAllByBookId() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        List<Receipt> receipts = new ArrayList<>();
        receipts.add(receipt);

        // when
        given(receiptRepository.getAllByBookId(receipt.getBookId()))
                .willReturn(receipts);

        assertThat(underTestReceiptService.getAllByBookId(receipt.getBookId()))
                .isEqualTo(receipts);

    }

    @Test
    void getAllByBookIdInCaseOfEmptyList() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        // when
        given(receiptRepository.getAllByBookId(receipt.getBookId()))
                .willReturn(Collections.emptyList());

        assertThat(underTestReceiptService.getAllByBookId(receipt.getBookId()))
                .isEqualTo(Collections.emptyList());
    }


    @Test
    void getAllByOrderStatus() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        List<Receipt> receipts = new ArrayList<>();
        receipts.add(receipt);

        // when
        given(receiptRepository.getAllByReceiptStatus(receipt.getReceiptStatus()))
                .willReturn(receipts);

        assertThat(underTestReceiptService.getAllByOrderStatus(receipt.getReceiptStatus()))
                .isEqualTo(receipts);

    }

    @Test
    void getAllByOrderStatusInCaseOfEmptyList() {

        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        // when
        given(receiptRepository.getAllByReceiptStatus(receipt.getReceiptStatus()))
                .willReturn(Collections.emptyList());

        assertThat(underTestReceiptService.getAllByOrderStatus(receipt.getReceiptStatus()))
                .isEqualTo(Collections.emptyList());

    }


    @Test
    void getAllByBookIdAndUserIdAndReceiptStatus() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        List<Receipt> receipts = new ArrayList<>();
        receipts.add(receipt);

        // when
        given(receiptRepository.getAllByBookIdAndUserIdAndReceiptStatus(
                receipt.getBookId(), receipt.getUserId(), receipt.getReceiptStatus()))
                .willReturn(receipts);

        assertThat(underTestReceiptService.getAllByBookIdAndUserIdAndReceiptStatus(
                receipt.getBookId(), receipt.getUserId(),receipt.getReceiptStatus()))
                .isEqualTo(receipts);

    }

    @Test
    void getAllByBookIdAndUserIdAndReceiptStatusInCaseOfEmptyList() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        // when
        given(receiptRepository.getAllByBookIdAndUserIdAndReceiptStatus(
                receipt.getBookId(), receipt.getUserId(), receipt.getReceiptStatus()))
                .willReturn(Collections.emptyList());

        assertThat(underTestReceiptService.getAllByBookIdAndUserIdAndReceiptStatus(
                receipt.getBookId(), receipt.getUserId(),receipt.getReceiptStatus()))
                .isEqualTo(Collections.emptyList());

    }


    @Test
    void CanGetByISBN() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);
        givenBook.setId(1L);
        givenBook.setIsbn(1L);

        // when
        given(receiptRepository.getByISBN(String.valueOf(givenBook.getIsbn())))
                .willReturn(receipt);

        // then
        assertThat(underTestReceiptService.getByISBN("1")).isEqualTo(receipt);

    }

    @Test
    void CanNotGetByISBN() {
        assertThat(underTestReceiptService.getByISBN("1")).isEqualTo(null);
    }


    @Test
    void CanGetById() throws NotFoundException {

        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        doReturn(Optional.of(receipt)).when(receiptRepository).findById(1L);

        //when
        Receipt result = underTestReceiptService.getById(1L);

        //then
        verify(receiptRepository).findById(1L);
        assertThat(receipt).isEqualTo(result);

    }

    @Test
    void CanNotGetById() {

        //given
        long receiptId = 1L;

        //then
        assertThatThrownBy(() -> underTestReceiptService.getById(receiptId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Receipt not found by the id " + receiptId);
    }


    @Test
    void save() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        //when
        underTestReceiptService.save(receipt);

        //then
        ArgumentCaptor<Receipt> receiptArgumentCaptor =
                ArgumentCaptor.forClass(Receipt.class);

        verify(receiptRepository)
                .save(receiptArgumentCaptor.capture());

        Receipt capturedReceipt = receiptArgumentCaptor.getValue();

        assertThat(capturedReceipt).isEqualTo(receipt);

    }

    @Test
    void getReceiptByBookIdAndUserIdAndReceiptStatus() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        // when
        given(receiptRepository.getReceiptByBookIdAndUserIdAndReceiptStatus(
                receipt.getBookId(), receipt.getUserId(), receipt.getReceiptStatus()))
                .willReturn(receipt);

        assertThat(underTestReceiptService.getReceiptByBookIdAndUserIdAndReceiptStatus(
                receipt.getBookId(), receipt.getUserId(),receipt.getReceiptStatus()))
                .isEqualTo(receipt);
    }

    @Test
    void getReceiptByBookIdAndUserIdAndReceiptStatusInCaseOfNull() {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        // when
        given(receiptRepository.getReceiptByBookIdAndUserIdAndReceiptStatus(
                receipt.getBookId(), receipt.getUserId(), receipt.getReceiptStatus()))
                .willReturn(null);

        assertThat(underTestReceiptService.getReceiptByBookIdAndUserIdAndReceiptStatus(
                receipt.getBookId(), receipt.getUserId(),receipt.getReceiptStatus()))
                .isEqualTo(null);
    }


    @Test
    void CanRemoveById() throws NotFoundException {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        doReturn(Optional.of(receipt)).when(receiptRepository).findById(receipt.getId());

        //when
        underTestReceiptService.removeById(receipt.getId());

        //then
        verify(receiptRepository).deleteById(receipt.getId());

    }

    @Test
    void CanNotRemoveById() {
        //given
        long givenReceiptId = 1L;

        //then
        assertThatThrownBy(() -> underTestReceiptService.removeById(givenReceiptId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Receipt not found by the id " + givenReceiptId);

    }


    @Test
    void CanUpdate() throws NotFoundException {
        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );

        when(receiptRepository.findById(receipt.getId()))
                .thenReturn(Optional.of(receipt));

        ArgumentCaptor<Receipt> argumentCaptor =
                ArgumentCaptor.forClass(Receipt.class);

        when(receiptRepository.save(argumentCaptor.capture()))
                .thenAnswer(iom -> iom.getArgument(0));


        // When
        receipt.setReceiptStatus(ReceiptStatus.EXPIRED);
        Receipt updated = underTestReceiptService.update(receipt);


        // Then
        assertThat(argumentCaptor.getValue().getReceiptStatus())
                .isEqualTo(updated.getReceiptStatus());

    }

    @Test
    void CanNotUpdate() {

        // given
        Receipt receipt = new Receipt(
                1L,
                1L,
                1L,
                1L,
                1L,
                ReceiptStatus.ACTIVE
        );


        //then
        assertThatThrownBy(() -> underTestReceiptService.update(receipt))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Receipt not found by the id " + receipt.getId());


    }

}