package com.app.library.service;

import com.app.library.exception.BadRequestException;
import com.app.library.exception.NotFoundException;
import com.app.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.mail.MessagingException;
import java.util.List;

public interface UserService {

    List<User> getAll();
    Page<User> getAll(Pageable pageable);
    Page<User> search(String keyword, Pageable pageable);
    User save(User user);
    User getByEmail(String email);
    User getById (Long id) throws NotFoundException;
    void removeById(Long id);
    User update(User user) throws NotFoundException;
    void verifyUserEmail(String email) throws NotFoundException;
    void upToResetPassword(String email) throws NotFoundException, MessagingException;
    void resetPassword(User user, String newPassword) throws NotFoundException, BadRequestException;
    void sendEmailAboutVerificationOfAccount(User user);
    User getByResetPasswordToken(String token);

}
