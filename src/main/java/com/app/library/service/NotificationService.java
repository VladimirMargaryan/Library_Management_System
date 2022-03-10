package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    List<Notification> getAll();
    List<Notification> getAllByUserId(Long id);
    Notification getById(Long id) throws NotFoundException;
    Notification save(Notification notification);
    void removeById(Long id);
    Notification update(Notification notification) throws NotFoundException;

}
