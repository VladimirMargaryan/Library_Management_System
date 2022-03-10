package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Notification;
import com.app.library.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    private NotificationServiceImpl underTestNotificationService;

    @BeforeEach
    void setUp() {
        underTestNotificationService = new NotificationServiceImpl(notificationRepository);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
    }


    @Test
    void getAll() {
        // when
        underTestNotificationService.getAll();

        // then
        verify(notificationRepository).findAll();
    }

    @Test
    void getAllByUserId() {
        // given
        long userId = 1L;

        // when
        underTestNotificationService.getAllByUserId(userId);

        // then
        verify(notificationRepository).getNotificationsByReceiverIdOrderByCreationDateDesc(userId);
    }

    @Test
    void CanGetById() throws NotFoundException {
        // given
        Notification notification = new Notification(
                1L,
                1L,
                "anyString",
                1L
        );

        doReturn(Optional.of(notification)).when(notificationRepository).findById(1L);

        //when
        Notification result = underTestNotificationService.getById(1L);

        //then
        assertThat(notification).isEqualTo(result);
    }

    @Test
    void CanNotGetById() throws NotFoundException {
        // given
        Notification notification = new Notification(
                1L,
                1L,
                "anyString",
                1L
        );

        //then
        assertThatThrownBy(() -> underTestNotificationService.getById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Notification not found by id " + 1L);
    }


    @Test
    void CanSave() {

        // given
        Notification notification = new Notification(
                1L,
                1L,
                "anyString",
                1L
        );

        //when
        underTestNotificationService.save(notification);

        //then
        ArgumentCaptor<Notification> notificationArgumentCaptor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository)
                .save(notificationArgumentCaptor.capture());

        Notification capturedNotification = notificationArgumentCaptor.getValue();

        assertThat(capturedNotification).isEqualTo(notification);

    }

    @Test
    void removeById() {
        // given
        long notificationId = 1L;

        // when
        underTestNotificationService.removeById(notificationId);

        // then
        verify(notificationRepository).deleteById(notificationId);

    }

    @Test
    void CanUpdate() throws NotFoundException {

        // given
        Notification notification = new Notification(
                1L,
                1L,
                "anyString",
                1L
        );

        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        ArgumentCaptor<Notification> argumentCaptor =
                ArgumentCaptor.forClass(Notification.class);

        when(notificationRepository.save(argumentCaptor.capture()))
                .thenAnswer(iom -> iom.getArgument(0));


        // When
        notification.setMessage("Fake Message");
        Notification updated = underTestNotificationService.update(notification);


        // Then
        assertThat(argumentCaptor.getValue().getMessage())
                .isEqualTo(updated.getMessage());

    }

    @Test
    void CanNotUpdate() {
        // given
        Notification notification = new Notification(
                1L,
                1L,
                "anyString",
                1L
        );

        // then
        assertThatThrownBy(() -> underTestNotificationService.update(notification))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Notification not found by id " + 1L);

    }

}