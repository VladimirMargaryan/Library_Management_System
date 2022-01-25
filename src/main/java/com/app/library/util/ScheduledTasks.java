package com.app.library.util;

import com.app.library.exception.NotFoundException;
import com.app.library.mail.MailSender;
import com.app.library.model.Book;
import com.app.library.model.BookStatus;
import com.app.library.model.Notification;
import com.app.library.model.User;
import com.app.library.service.BookService;
import com.app.library.service.NotificationService;
import com.app.library.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.DateFormat;
import java.util.List;

@Configuration
@EnableScheduling
@Slf4j
public class ScheduledTasks {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Async
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void deleteOldNotifications() {
        log.info("Start of scheduled task for deleting old notifications on "
                + DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));

        List<Notification> notifications = notificationService.getAll();
        for (Notification notification : notifications) {
            if (System.currentTimeMillis() > notification.getCreationDate() + 1000 * 60 * 60 * 24 * 3){
                notificationService.removeById(notification.getId());
            }
        }
    }

    @Async
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void sendEmailToUserAboutExpirationOfReturnDate() throws NotFoundException {
        log.info("Start of scheduled task 'sendEmailToUserAboutExpirationOfReturnDate' on "
                + DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));

        List<Book> books = bookService.getAllByStatus(BookStatus.CHECKED_OUT);
        if (books != null && !books.isEmpty()) {
            for (Book book : books) {
                if (book.getReturnDate() != null){
                    int twoDaysInMillis = 1000 * 60 * 60 * 24 * 2;
                    if (System.currentTimeMillis() - (book.getReturnDate() - twoDaysInMillis) >= 0){
                        User user = userService.getById(book.getUsedBy());
                        if (user != null) {
                            String subject = "reservation expires after 2 days";
                            String message = "Dear " + user.getFirstname() + ", your taken book's"
                                    + "date expires after 2 days, on " + DateFormat.getDateTimeInstance().format(book.getReservedUntil()) + ". "
                                    + "Please extend time or return the book to library.";
                            mailSender.sendSimpleMessage(user.getEmail(), subject, message);
                            Notification notification = new Notification(System.currentTimeMillis(), message, book.getUsedBy());
                            notificationService.save(notification);
                        }
                    }
                }
            }
        }
    }

    @Async
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void cancelReservationIfNotPickedUp() throws NotFoundException {
        log.info("Start of scheduled task for canceling reservations of not picked up books on "
                + DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));

        List<Book> books = bookService.getAllByStatus(BookStatus.READY_FOR_PICK_UP);
        for (Book book : books) {
            if (System.currentTimeMillis() > book.getReservedUntil()) {
                User user = userService.getById(book.getReservedBy());
                if (user != null) {
                    String subject = "Reservation canceled";
                    String message = "Dear " + user.getFirstname() + ",\n Your reservation is canceled " +
                            "because you don't pick up the book \"" + book.getName() + "\" by " +
                            book.getAuthors().get(0).getFirstname() + " " + book.getAuthors().get(0).getLastname();
                    mailSender.sendSimpleMessage(user.getEmail(), subject, message);
                    Notification notification = new Notification(System.currentTimeMillis(), message, book.getReservedBy());
                    notificationService.save(notification);
                }
                book.setBookStatus(BookStatus.CHECKED_IN);
                book.setReservedUntil(null);
                book.setReservedBy(null);
                bookService.update(book);
            }
        }
    }
}
