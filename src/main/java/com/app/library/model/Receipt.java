package com.app.library.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "receipt")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "orderDate")
    private Long orderDate;

    @Column(name = "expirationDate")
    private Long expirationDate;

    @Column(name = "user_id")
    @NotNull
    private Long userId;

    @Column(name = "book_id")
    @NotNull
    private Long bookId;

    @Column(name = "receiptStatus")
    @Enumerated(EnumType.STRING)
    @NotNull
    private ReceiptStatus receiptStatus;

    public Receipt(Long orderDate, Long expirationDate, Long userId, Long bookId, ReceiptStatus receiptStatus) {
        this.orderDate = orderDate;
        this.expirationDate = expirationDate;
        this.userId = userId;
        this.bookId = bookId;
        this.receiptStatus = receiptStatus;
    }

    public Receipt(Long userId, Long bookId, ReceiptStatus receiptStatus) {
        this.userId = userId;
        this.bookId = bookId;
        this.receiptStatus = receiptStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Receipt receipt = (Receipt) o;
        return id != null && Objects.equals(id, receipt.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
