package com.app.library.repository;

import com.app.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User getByEmail(String email);
    User getByResetPasswordToken(String token);
    @Query("select u from User u where concat(u.firstname, ' ', u.lastname, ' ', u.email, ' ', u.gender, ' ', u.phone) like %?1%")
    Page<User> search(String keyword, Pageable pageable);
}
