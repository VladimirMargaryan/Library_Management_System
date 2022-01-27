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
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Book> booksThatAlsoReserved = bookService.getAllByStatus(BookStatus.CHECKED_OUT_AND_RESERVED);
        if (booksThatAlsoReserved != null && !booksThatAlsoReserved.isEmpty())
            books.addAll(booksThatAlsoReserved);

        List<Book> booksWithExpiration = new ArrayList<>();
        if (books != null && !books.isEmpty()) {
            for (Book book : books) {
                if (book.getReturnDate() != null){
                    int twoDaysInMillis = 1000 * 60 * 60 * 24 * 2;
                    if (System.currentTimeMillis() - (book.getReturnDate() - twoDaysInMillis) >= 0){
                        booksWithExpiration.add(book);
                        User user = userService.getById(book.getUsedBy());
                        if (user != null) {
                            String message = "Dear " + user.getFirstname() + ", your taken book's"
                                    + "date expires after 2 days, on " + DateFormat.getDateTimeInstance().format(book.getReservedUntil()) + ". "
                                    + "Please extend time or return the book to library.";
                            Notification notification = new Notification(System.currentTimeMillis(), message, book.getUsedBy());
                            notificationService.save(notification);
                        }
                    }
                }
            }
            while (!booksWithExpiration.isEmpty()){
                long userId = booksWithExpiration.get(0).getUsedBy();
                List<Book> oneUserBooksWithExpiration = booksWithExpiration
                        .stream()
                        .filter(book -> book.getUsedBy().equals(userId))
                        .collect(Collectors.toList());
                booksWithExpiration.removeIf(book -> book.getUsedBy().equals(userId));

                String date = DateFormat.getDateInstance().format(System.currentTimeMillis());
                User user = userService.getById(userId);

                try {
                    Context context = new Context();
                    context.setVariable("date", date);
                    context.setVariable("name", user.getFirstname());
                    context.setVariable("books", oneUserBooksWithExpiration);

                    mailSender.sendEmailUsingTemplate(user.getEmail(), "reservation expires after 2 days",
                            "return-date-expires-after-two-days", context);
                    log.info("email sent.");
                } catch (MessagingException e) {
                    log.error("error while sending an email");
                    e.printStackTrace();
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
                    String message = "Dear " + user.getFirstname() + ",\n Your reservation is canceled " +
                            "because you don't pick up the book \"" + book.getName() + "\" by " +
                            book.getAuthors().get(0).getFirstname() + " " + book.getAuthors().get(0).getLastname();

                    try {
                        Context context = new Context();
                        context.setVariable("book", book);
                        context.setVariable("name", user.getFirstname());

                        mailSender.sendEmailUsingTemplate(user.getEmail(), "Reservation canceled",
                                "reservation-canceled", context);
                        log.info("Email sent to " + user.getEmail());
                    } catch (MessagingException e) {
                        log.error("Error while sending an email to " + user.getEmail());
                        e.printStackTrace();
                    }
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
