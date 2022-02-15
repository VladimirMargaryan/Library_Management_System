package com.app.library.service;


import com.app.library.exception.NotFoundException;
import com.app.library.mail.MailSender;
import com.app.library.model.*;
import com.app.library.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.app.library.model.BookStatus.*;

@Service
@Slf4j
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private ReceiptService receiptService;



    @Override
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    @Override
    public Page<Book> getAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    public Page<Book> search(String keyword, Pageable pageable) {
        return bookRepository.findByAndSort(keyword, pageable);
    }

    @Override
    public List<Book> getAllByStatus(BookStatus status) {
        return bookRepository.getAllByBookStatus(status);
    }


    @Transactional
    @Override
    public Book save(Book book) {
        if (book.getBookStatus() == null)
            book.setBookStatus(BookStatus.CHECKED_IN);
        if (book.getIsbn() == null) {
            long isbn = (long) Math.floor(Math.random() * 9000000000000L) + 1000000000000L;
            book.setIsbn(isbn);
        }
        Book saved = bookRepository.save(book);
        log.info("Book saved! " + saved);
        return saved;
    }

    @Transactional
    @Override
    public Book save(Book book, Author author) {
        book.setBookStatus(BookStatus.CHECKED_IN);
        long isbn = (long) Math.floor(Math.random() * 9000000000000L) + 1000000000000L;
        Book foundedBook = bookRepository.getByIsbn(isbn);
        if (foundedBook != null) {
            log.info("Book founded by the isbn " + isbn);
            isbn = (long) Math.floor(Math.random() * 9000000000000L) + 1000000000000L;
            log.info("isbn changed. isbn: " + isbn);

        }
        book.setIsbn(isbn);
        List<Author> authors = book.getAuthors();
        if (authors == null)
            authors = new ArrayList<>();
        authors.add(author);
        book.setAuthors(authors);
        Book duplicateBook = bookRepository.getBookByName(book.getName());
        if (duplicateBook != null) {
            log.info("Duplicate book is founded. " + duplicateBook);
            Optional<Author> foundedAuthor = duplicateBook.getAuthors().stream().filter(author1 ->
                    author1.getFirstname().equals(author.getFirstname()) &&
                            author1.getLastname().equals(author.getLastname())).findFirst();
            if (foundedAuthor.isPresent()){
                return null;
            }
        }
        Book saved = bookRepository.save(book);
        log.info("Book saved. " + saved);
        return saved;
    }

    @Override
    public Book getById(Long id) throws NotFoundException {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (!optionalBook.isPresent()) {
            log.error("Book not found by the id " + id);
            throw new NotFoundException("Book not found by the id " + id);
        }
        log.info("Book founded by the id " + id);
        return optionalBook.get();
    }


    @Transactional
    @Override
    public void removeById(Long id) {
        try {
            Book founded = getById(id);
        } catch (NotFoundException e) {
            log.error("Book not found by id " + id);
            e.printStackTrace();
        }
        bookRepository.deleteById(id);
        log.info("Book deleted!");
    }

    @Transactional
    @Override
    public Book update(Book book) throws NotFoundException {
        Book newBook = getById(book.getId());
        newBook.setName(book.getName());
        newBook.setGenre(book.getGenre());
        newBook.setIsbn(book.getIsbn());
        newBook.setPhoto(book.getPhoto());
        newBook.setReleaseYear(book.getReleaseYear());
        newBook.setBookStatus(book.getBookStatus());
        newBook.setUsedBy(book.getUsedBy());
        newBook.setReservedBy(book.getReservedBy());
        newBook.setReservedUntil(book.getReservedUntil());
        newBook.setAuthors(book.getAuthors());
        newBook.setReturnDate(book.getReturnDate());
        Book updated = save(newBook);
        log.info("Book updated! " + updated);
        return updated;
    }

    @Override
    public List<Book> getAllByUsedBy(Long id) {
        return bookRepository.getAllByUsedBy(id);
    }

    @Override
    public Page<Book> getAllByUsedBy(Long id, Pageable pageable) {
        return bookRepository.getAllByUsedBy(id, pageable);
    }

    @Override
    public Page<Book> getAllByReservedBy(Long id, Pageable pageable) {
        return bookRepository.getAllByReservedBy(id, pageable);
    }


    @Override
    public Page<Book> getAllByBookStatusAndReservedBy(BookStatus bookStatus, Long reservedBy, Pageable pageable) {
        return bookRepository.getBookByBookStatusAndReservedBy(bookStatus, reservedBy, pageable);
    }

    @Override
    public Page<Book> getBooksByUserIdAndWithExpirationDate(Long userId, Long currentTime, Pageable pageable) {
        return bookRepository.getBooksByUserIdAndWithExpirationDate(userId, currentTime, pageable);
    }

    @Override
    public List<Book> getAllByBookStatusAndReservedBy(BookStatus bookStatus, Long reservedBy) {
        return bookRepository.getBookByBookStatusAndReservedBy(bookStatus, reservedBy);
    }

    @Transactional
    @Override
    public void doReservation(Book book, User user) throws NotFoundException {
        if (!book.getBookStatus().equals(CHECKED_OUT_AND_RESERVED) && !book.getBookStatus().equals(RESERVED) && book.getReservedBy() == null) {
            log.info("Reservation of " + book + " by user " + user);

            book.setReservedBy(user.getId());
            if (book.getBookStatus().equals(CHECKED_OUT))
                book.setBookStatus(CHECKED_OUT_AND_RESERVED);
            else if (book.getBookStatus().equals(CHECKED_IN))
                book.setBookStatus(RESERVED);
            book.setReservedUntil(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7);

            String messageToEmployee = user.getFirstname() + " " + user.getLastname() + " reserved a book \"" +
                    book.getName() + "\" by " + book.getAuthors().get(0).getFirstname() + " " +
                    book.getAuthors().get(0).getLastname() + ", ISBN: " + book.getIsbn() + ".";

            String messageToUser = "You reserved a book \"" + book.getName() + "\" by " +
                    book.getAuthors().get(0).getFirstname() + " " + book.getAuthors().get(0).getLastname() +
                    ". You will receive a notification for picking it up.";

            List<User> users = userService.getAll();

            List<User> employees = users.stream().filter(user1 -> user1.getRoles().stream().
                    anyMatch(role -> Objects.equals(role.getName(), "ROLE_EMPLOYEE"))).collect(Collectors.toList());

            for (User employee : employees) {
                Notification notificationToEmployee = new Notification(System.currentTimeMillis(), messageToEmployee);
                notificationToEmployee.setReceiverId(employee.getId());
                notificationService.save(notificationToEmployee);
            }

            Notification notificationToUser = new Notification(System.currentTimeMillis(), messageToUser);
            notificationToUser.setReceiverId(user.getId());
            notificationService.save(notificationToUser);
            update(book);
        }
    }

    @Transactional
    @Override
    public void notifyingForPickingBookUp(Book book, User user) throws NotFoundException {
        log.info("Notifying for pick up book " + book + " to user " + user);

        String messageToUser = "Your reservation is ready for pick-up until " +
                DateFormat.getDateTimeInstance().format(book.getReservedBy()) + ". \"" + book.getName() + " by " +
                book.getAuthors().get(0).getFirstname() + " " + book.getAuthors().get(0).getLastname() + ".";

        Notification notification = new Notification(System.currentTimeMillis(), messageToUser);
        notification.setReceiverId(user.getId());
        notificationService.save(notification);

        List<Book> books = new ArrayList<>();
        books.add(book);
        sendReservationReadyEmail(books, user);
        book.setBookStatus(READY_FOR_PICK_UP);
        update(book);
    }

    @Transactional
    @Override
    public void notifyingUserForPickingUpBooks(List<Book> books, User user) throws NotFoundException {
        log.info("Notifying for pick up book " + books + " to user " + user);

        for (Book book : books){
            String messageToUser = "Your reservation is ready for pick-up until " +
                    DateFormat.getDateTimeInstance().format(book.getReservedBy()) + ". \"" + book.getName() + " by " +
                    book.getAuthors().get(0).getFirstname() + " " + book.getAuthors().get(0).getLastname() + ".";

            Notification notification = new Notification(System.currentTimeMillis(), messageToUser);
            notification.setReceiverId(user.getId());
            notificationService.save(notification);

            book.setBookStatus(READY_FOR_PICK_UP);
            update(book);
        }
        sendReservationReadyEmail(books, user);
    }

    @Override
    public void sendReservationReadyEmail(List<Book> books, User user) {
        String date = DateFormat.getDateTimeInstance().format(System.currentTimeMillis());
        Map<String, Object> map = new HashMap<>();
        map.put("books", books);
        map.put("user", user);
        map.put("date", date);
        log.info("Sending reservation email to user " + user);
        try {
            Context context = new Context();
            context.setVariables(map);
            mailSender.sendEmailUsingTemplate(user.getEmail(),"Reservation is ready!",
                    "reservation-template", context);
        } catch (MessagingException e) {
            log.error("Email doesn't send: " + e.getMessage());
            e.printStackTrace();
        }
        log.info("Email sent.");
    }

    @Override
    public void sendPickUpAndReceiptEmail(Long userId, List<Receipt> receipts) throws NotFoundException {
        long receiptN = 0;
        String orderDate = null;
        String expirationDate = null;
        String returnDate = null;

        Map<String, Object> map = new HashMap<>();
        List<Book> books = new ArrayList<>();

        User user = userService.getById(userId);

        for (Receipt receipt : receipts) {
            Book book = getById(receipt.getBookId());

            if (book != null) {
                books.add(book);
                returnDate = DateFormat.getDateInstance().format(book.getReturnDate());
            }

            receiptN += receipt.getId();
            orderDate = DateFormat.getDateTimeInstance().format(receipt.getOrderDate());
            expirationDate = DateFormat.getDateTimeInstance().format(receipt.getExpirationDate());
        }

        map.put("user", user);
        map.put("receiptNumber", receiptN);
        map.put("orderDate", orderDate);
        map.put("returnDate", returnDate);
        map.put("pickedUp", DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
        map.put("expirationDate", expirationDate);
        map.put("books", books);

        log.info("Sending pick up and receipt email to user " + user);
        try {
            Context context = new Context();
            context.setVariables(map);
            mailSender.sendEmailUsingTemplate(user.getEmail(), "Receipt about picking up book!",
                    "receipt-template", context);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        log.info("Email sent.");
    }

    @Transactional
    @Override
    public void doPickUp(Long userId) throws NotFoundException {
        log.info("Picking up book/books by user - id = " + userId);
        List<Book> selectedBooks = getAllByBookStatusAndReservedBy(BookStatus.READY_FOR_PICK_UP, userId);
        List<Receipt> receipts = new ArrayList<>();
        for (Book book : selectedBooks) {
            book.setBookStatus(CHECKED_OUT);
            book.setUsedBy(userId);
            book.setReservedBy(null);
            book.setReservedUntil(null);
            book.setReturnDate(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 15);
            update(book);
            long dateTimeMillis = System.currentTimeMillis();
            Receipt receipt = new Receipt(
                    dateTimeMillis,
                    dateTimeMillis + 1000 * 60 * 60 * 24 * 15,
                    userId, book.getId(), ReceiptStatus.ACTIVE);
            Receipt savedReceipt = receiptService.save(receipt);
            receipts.add(savedReceipt);
        }
        sendPickUpAndReceiptEmail(userId, receipts);
    }

    @Transactional
    @Override
    public void doReturnBooks(Set<Book> books, Long userId) throws NotFoundException {
        log.info("Returning book/books by user - id = " + userId);
        for (Book book : books) {
            Receipt receipt = receiptService.getReceiptByBookIdAndUserIdAndReceiptStatus(book.getId(),
                    book.getUsedBy(), ReceiptStatus.ACTIVE);
            receipt.setReceiptStatus(ReceiptStatus.OUT_OF_USE);
            receiptService.update(receipt);

            if (book.getReservedBy() != null)
                book.setBookStatus(RESERVED);
            else
                book.setBookStatus(CHECKED_IN);
            book.setReturnDate(null);
            book.setUsedBy(null);
            update(book);
            String notificationMessage = "You returned a book \"" + book.getName() + "\" by " +
                    book.getAuthors().get(0).getFirstname() + " " + book.getAuthors().get(0).getLastname();
            Notification notification = new Notification(System.currentTimeMillis(),notificationMessage, userId);
            notificationService.save(notification);
        }
        sendEmailAboutReturnedBook(userId, books);
    }

    @Override
    public void sendEmailAboutReturnedBook(Long userId, Set<Book> books) throws NotFoundException {
        User user = userService.getById(userId);
        String date = DateFormat.getDateTimeInstance().format(System.currentTimeMillis());
        Map<String, Object> map = new HashMap<>();
        map.put("date", date);
        map.put("user", user);
        map.put("books", books);

        try {
            Context context = new Context();
            context.setVariables(map);
            mailSender.sendEmailUsingTemplate(user.getEmail(), "Returned books!",
                    "returned-books-template", context);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        log.info("Email sent.");
    }

    @Override
    public Page<Book> findPaginated(Pageable pageable, Collection<Book> books) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<Book> list = new ArrayList<>();

        if (books.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, books.size());
            int i = 0;
            for (Book book : books){
                if (i >= startItem && i < toIndex)
                    list.add(book);
                i++;
            }
        }
        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), books.size());
    }

    @Override
    public Page<ListEntry<Book, User>> findPaginated(Pageable pageable, Map<Book, User> bookUserMap){
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<ListEntry<Book, User>> list = new ArrayList<>();

        if (bookUserMap.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, bookUserMap.size());
            int i = 0;
            for (Map.Entry<Book, User> entry : bookUserMap.entrySet()){
                if (i >= startItem && i < toIndex)
                    list.add(new ListEntry<>(entry.getKey(), entry.getValue()));
                i++;
            }
        }
        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), bookUserMap.size());

    }
}
