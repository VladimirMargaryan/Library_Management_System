package com.app.library.model;


import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "book")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "book_genre")
    @NotNull
    @Enumerated(EnumType.STRING)
    private BookGenre genre;

    @Column(name = "release_year")
    @NotNull
    private int releaseYear;

    @Column(name = "isbn", unique = true, updatable = false)
    @NotNull
    private Long isbn;

    @Column(name = "bookStatus")
    @NotNull
    @Enumerated(EnumType.STRING)
    private BookStatus bookStatus;

    @Column(name = "usedByUser")
    private Long usedBy;

    @Column(name = "reservedBy")
    private Long reservedBy;

    @Column(name = "reservedUntil")
    private Long reservedUntil;

    @Column(name = "returnDate")
    private Long returnDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    private List<Author> authors;


    public Book(String name, BookGenre genre) {
        this.name = name;
        this.genre = genre;
    }

    public Book(String name, BookGenre genre, int releaseYear) {
        this.name = name;
        this.genre = genre;
        this.releaseYear = releaseYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Book book = (Book) o;
        return id != null && Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", genre=" + genre +
                ", releaseYear=" + releaseYear +
                ", isbn=" + isbn +
                ", bookStatus=" + bookStatus +
                ", usedBy=" + usedBy +
                ", reservedBy=" + reservedBy +
                ", reservedUntil=" + reservedUntil +
                ", returnDate=" + returnDate +
                ", authors=" + authors +
                '}';
    }
}
