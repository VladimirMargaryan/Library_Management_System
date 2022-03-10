package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Notification;
import com.app.library.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> getAllByUserId(Long id) {
        return notificationRepository.getNotificationsByReceiverIdOrderByCreationDateDesc(id);
    }


    @Override
    public Notification getById(Long id) throws NotFoundException {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (!optionalNotification.isPresent()){
            log.error("Notification not found by id " + id);
            throw new NotFoundException("Notification not found by id " + id);
        }
        log.info("Notification founded! " + optionalNotification.get());
        return optionalNotification.get();
    }

    @Override
    public Notification save(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        log.info("Notification saved! " + saved);
        return saved;
    }

    @Override
    public void removeById(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public Notification update(Notification notification) throws NotFoundException {
        Notification newNotification = getById(notification.getId());
        newNotification.setCreationDate(notification.getCreationDate());
        newNotification.setMessage(notification.getMessage());
        newNotification.setReceiverId(notification.getReceiverId());
        Notification updated = save(newNotification);
        log.info("Notification updated! " + updated);
        return updated;
    }
}
