package com.app.library.repository;

import com.app.library.exception.NotFoundException;
import com.app.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User getByEmail(String email);
    User getByEmailAndPassword(String email, String password);
    User getByResetPasswordToken(String token);
    @Query("select u from User u where concat(u.firstname, ' ', u.lastname, ' ', u.email, ' ', u.gender, ' ', u.phone) like %?1%")
    Page<User> search(String keyword, Pageable pageable);
    List<User> getAllByFirstnameAndLastname(String firstName, String lastName);
    List<User> getAllByFirstname(String firstname);
    List<User> getAllByLastname(String lastname);
}
