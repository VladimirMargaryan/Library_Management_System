package com.app.library.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "notification")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "creation_date")
    private Long creationDate;

    @Column(name = "message")
    @NotNull
    private String message;

    @Column(name = "receiverId")
    @NotNull
    private Long receiverId;

    public Notification(Long creationDate, String message) {
        this.creationDate = creationDate;
        this.message = message;
    }

    public Notification(Long creationDate, String message, Long receiverId) {
        this.creationDate = creationDate;
        this.message = message;
        this.receiverId = receiverId;
    }

    public Notification(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, message);
    }
}
