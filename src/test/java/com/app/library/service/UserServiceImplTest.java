package com.app.library.service;

import com.app.library.exception.BadRequestException;
import com.app.library.exception.NotFoundException;
import com.app.library.mail.MailSender;
import com.app.library.model.Book;
import com.app.library.model.Role;
import com.app.library.model.User;
import com.app.library.model.UserStatus;
import com.app.library.repository.UserRepository;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailSender mailSender;
    @Rule
    ExpectedException exception = ExpectedException.none();


    private UserServiceImpl underTestUserService;

    @BeforeEach
    void setUp() {
        underTestUserService = new UserServiceImpl(
                userRepository, roleService, passwordEncoder, mailSender);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void getAll() {
        // when
        underTestUserService.getAll();

        // then
        verify(userRepository).findAll();
    }

    @Test
    void testGetAll() {

        // given
        PageRequest pageable = PageRequest.of(3, 5);

        // when
        underTestUserService.getAll(pageable);

        // then
        verify(userRepository).findAll(pageable);
    }

    @Test
    void search() {
        // given
        PageRequest pageable = PageRequest.of(3, 5);
        String givenKeyword = "keyword";

        // when
        underTestUserService.search(givenKeyword, pageable);

        // then
        verify(userRepository).search(givenKeyword, pageable);
    }

    @Test
    void save() {
        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("FakePassword");
        user.setFirstname("name");
        user.setEmail("FakeEmail");

        Role role = new Role(1L, "ROLE_USER");

        given(passwordEncoder.encode(user.getPassword())).willReturn("EncodedPassword");
        given(roleService.getByName("ROLE_USER")).willReturn(role);

        // when
        underTestUserService.save(user);

        // then
        assertThat(user.getPassword()).isEqualTo("EncodedPassword");
        assertThat(user.getRoles().contains(role)).isEqualTo(true);
        verify(userRepository).save(user);
    }

    @Test
    @Disabled
    void CannotSendEmailAboutVerificationOfAccountAndThrowMessagingException(){
    }

    @Test
    void verifyUserEmail() throws NotFoundException {
        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("FakePassword");
        user.setFirstname("name");
        user.setEmail("FakeEmail");
        user.setStatus(UserStatus.UNVERIFIED);

        given(userRepository.getByEmail(user.getEmail())).willReturn(user);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        ArgumentCaptor<User> argumentCaptor =
                ArgumentCaptor.forClass(User.class);

        when(userRepository.save(argumentCaptor.capture()))
                .thenAnswer(iom -> iom.getArgument(0));


        // when
        underTestUserService.verifyUserEmail(user.getEmail());
        User updated = underTestUserService.update(user);

        // then
        assertThat(argumentCaptor.getValue().getStatus())
                .isEqualTo(updated.getStatus());
        verify(userRepository, times(2)).save(user);
        verify(userRepository).getByEmail(user.getEmail());

    }

    @Test
    void userNotFoundDuringVerificationAndThrowsAnException() {

        // then
        assertThatThrownBy(() -> underTestUserService.verifyUserEmail("FakeEmail"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found by the email ");
    }

    @Test
    void userNotFoundForBeingVerifiedAndThrowsException() throws NotFoundException {
        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("FakePassword");
        user.setFirstname("name");
        user.setEmail("FakeEmail");
        user.setStatus(UserStatus.UNVERIFIED);

        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> underTestUserService.update(user))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found by the id " + user.getId());

    }

    @Test
    @Disabled
    void getByEmail() {
    }

    @Test
    @Disabled
    void getById() {
    }

    @Test
    void CanRemoveById() throws NotFoundException {
        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("FakePassword");
        user.setFirstname("name");
        user.setEmail("FakeEmail");
        user.setStatus(UserStatus.UNVERIFIED);

        doReturn(Optional.of(user)).when(userRepository).findById(user.getId());

        //when
        underTestUserService.removeById(user.getId());

        //then
        verify(userRepository).deleteById(user.getId());
    }

    @Test
    void CannotRemoveByIdAndThrowsException(){
        assertThatThrownBy(() -> underTestUserService.removeById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found by the id ");

    }

    @Test
    @Disabled
    void update() {
    }

    @Test
    void upToResetPassword() throws NotFoundException, MessagingException {
        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("FakePassword");
        user.setFirstname("name");
        user.setEmail("FakeEmail");
        user.setStatus(UserStatus.VERIFIED);

        given(underTestUserService.getByEmail(user.getEmail())).willReturn(user);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        underTestUserService.upToResetPassword(user.getEmail());

        // then
       verify(mailSender).sendEmailUsingTemplate(any(String.class), any(String.class), any(String.class), any(Context.class));
       verify(userRepository).save(user);
       verify(userRepository).findById(user.getId());
    }

    @Test
    void userNotFoundByTheEmailDuringResetPassword() {
        assertThatThrownBy(() -> underTestUserService.upToResetPassword("FakeEmail"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found by the email ");
    }

    @Test
    void resetPassword() throws NotFoundException, BadRequestException {
        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("FakePassword");
        user.setFirstname("name");
        user.setEmail("FakeEmail");
        user.setStatus(UserStatus.VERIFIED);
        user.setResetPasswordTokeCreationDate(System.currentTimeMillis());

        given(passwordEncoder.encode(user.getPassword())).willReturn(user.getPassword());
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        underTestUserService.resetPassword(user, user.getPassword());

        // then
        assertThat(passwordEncoder.encode(user.getPassword())).isEqualTo(user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void KeyTimeExpiredAndThrowsBadRequestException() {
        // given
        User user = new User();
        user.setPassword("FakePassword");
        user.setResetPasswordTokeCreationDate(1000L);

        // then
        assertThatThrownBy(() -> underTestUserService.resetPassword(user, user.getPassword()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Your key's time expired!");

    }

    @Test
    void CanGetByResetPasswordToken() throws NotFoundException {
        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("FakePassword");
        user.setFirstname("name");
        user.setEmail("FakeEmail");
        user.setStatus(UserStatus.VERIFIED);
        user.setResetPasswordTokeCreationDate(System.currentTimeMillis());
        user.setResetPasswordToken("FakeToken");

        given(userRepository.getByResetPasswordToken(user.getResetPasswordToken()))
                .willReturn(user);

        // when
        User founded = underTestUserService.getByResetPasswordToken("FakeToken");

        // then
        assertThat(founded).isEqualTo(user);
    }

    @Test
    void CannotGetByTokenAndThrowsNotFoundException() {
        // given
        String givenToken = "FakeToken";

        // then
        assertThatThrownBy(() -> underTestUserService.getByResetPasswordToken(givenToken))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with reset password token ");

    }
}