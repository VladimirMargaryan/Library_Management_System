package com.app.library.service;

import com.app.library.exception.BadRequestException;
import com.app.library.exception.NotFoundException;
import com.app.library.mail.MailSender;
import com.app.library.model.User;
import com.app.library.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.util.*;

import static com.app.library.model.UserStatus.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailSender mailSender;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> search(String keyword, Pageable pageable) {
        return userRepository.search(keyword, pageable);
    }


    @Transactional
    @Override
    public User save(User user) {
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        user.setLocalDateTime(System.currentTimeMillis());
        user.setStatus(UNVERIFIED);
        user.getRoles().add(roleService.getByName("ROLE_USER"));
        userRepository.save(user);
        log.info("New user saved! " + user);
        sendEmailAboutVerificationOfAccount(user);
        return user;
    }

    @Override
    public void sendEmailAboutVerificationOfAccount(User user) {
        String link = "http://localhost:8080/verify?email=" + user.getEmail();
        String subject = "Account Verification";
        log.info("Sending email about verifying account!");
        try {
            Context context = new Context();
            context.setVariable("name", user.getFirstname());
            context.setVariable("url", link);
            mailSender.sendEmailUsingTemplate(user.getEmail(), subject,"account-verification-template", context);
            log.info("Email sent to user email " + user.getEmail());
        } catch (MessagingException e) {
            log.error("Error occurred while sending an email to email " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void verifyUserEmail(String email) throws NotFoundException {
        User user = userRepository.getByEmail(email);
        if (user == null)
            throw new NotFoundException("User not found by the email " + email);
        user.setStatus(VERIFIED);
        update(user);
        log.info("User account verified!");
    }


    @Override
    public User getByEmail(String email) {
        User user = userRepository.getByEmail(email);
        if (user == null)
            log.info("User not found by the email " + email);
        return user;
    }

    @Override
    public User getById(Long id) throws NotFoundException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            log.error("User not found by the id " + id);
            throw new NotFoundException("User not found by the id " + id);
        }
        log.info("User founded! " + optionalUser.get());
        return optionalUser.get();
    }


    @Transactional
    @Override
    public void removeById(Long id) {
        userRepository.deleteById(id);
        log.info("User deleted by the id " + id);
    }

    @Transactional
    @Override
    public User update(User user) throws NotFoundException {
        User newUser = getById(user.getId());
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setPhone(user.getPhone());
        newUser.setLocalDateTime(user.getLocalDateTime());
        newUser.setRoles(user.getRoles());
        newUser.setGender(user.getGender());
        newUser.setResetPasswordToken(user.getResetPasswordToken());
        newUser.setResetPasswordTokeCreationDate(user.getResetPasswordTokeCreationDate());
        newUser.setStatus(user.getStatus());
        User updated = userRepository.save(newUser);
        log.info("User updated! " + updated);
        return updated;
    }

    @Transactional
    @Override
    public void upToResetPassword(String email) throws NotFoundException {
        User user = getByEmail(email);
        if (user != null && user.getStatus().equals(VERIFIED)) {
            log.info("Preparing to reset password to user " + user);
            String token = RandomString.make(24);
            user.setResetPasswordToken(token);
            Long creationDate = System.currentTimeMillis();
            user.setResetPasswordTokeCreationDate(creationDate);
            String subject = "Reset password request";
            String link = "http://localhost:8080/reset_password?token=" + token;
            update(user);
            try {
                Context context = new Context();
                context.setVariable("name", user.getFirstname());
                context.setVariable("url", link);
                mailSender.sendEmailUsingTemplate(user.getEmail(), subject,"reset-password-template", context);
                log.info("Message sent to email " + user.getEmail());
            } catch (MessagingException e) {
                log.error("Can't send email to " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.error("User not found by email " + email);
            throw new NotFoundException("User not found by this email");
        }
    }

    @Transactional
    @Override
    public void resetPassword(User user, String newPassword) throws NotFoundException, BadRequestException {
        String encodedPassword = passwordEncoder.encode(newPassword);
        if (System.currentTimeMillis() - user.getResetPasswordTokeCreationDate() < 600000){
            user.setPassword(encodedPassword);
            user.setResetPasswordTokeCreationDate(null);
            user.setResetPasswordToken(null);
            update(user);
        } else {
            log.error("Expired reset password token " + user.getResetPasswordToken());
            throw new BadRequestException("Your key's time expired!");
        }
    }

    @Override
    public User getByResetPasswordToken(String token) {
        User user = userRepository.getByResetPasswordToken(token);
        if (user == null)
            log.info("User not found with reset password token " + token);
        return user;
    }
}
