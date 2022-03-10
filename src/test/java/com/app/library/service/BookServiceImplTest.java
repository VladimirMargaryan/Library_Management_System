package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.mail.MailSender;
import com.app.library.model.*;
import com.app.library.repository.BookRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.mail.MessagingException;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MailSender mailSender;
    @Mock
    private ReceiptService receiptService;

    private BookServiceImpl underTestBookService;


    @BeforeEach
    void setUp() {
        underTestBookService = new BookServiceImpl(
                bookRepository,
                userService,
                notificationService,
                mailSender,
                receiptService
        );

    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test
    void getAll() {
        //when
        underTestBookService.getAll();

        //then
        verify(bookRepository).findAll();
    }

    @Test
    void testGetAll() {
        // given
        PageRequest pageable = PageRequest.of(3, 5);

        // when
        underTestBookService.getAll(pageable);

        // then
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void search() {
        // given
        PageRequest pageable = PageRequest.of(3, 5);
        String givenKeyword = "keyword";

        // when
        underTestBookService.search(givenKeyword, pageable);

        // then
        verify(bookRepository).findByAndSort(givenKeyword, pageable);

    }

    @Test
    void getAllByStatus() {
        // given
        BookStatus givenStatus = any();

        // when
        underTestBookService.getAllByStatus(givenStatus);

        // then
        verify(bookRepository).getAllByBookStatus(givenStatus);
    }

    @Test
    void save() {

        // given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);

        //when
        underTestBookService.save(givenBook);

        //then
        ArgumentCaptor<Book> bookArgumentCaptor =
                ArgumentCaptor.forClass(Book.class);

        verify(bookRepository)
                .save(bookArgumentCaptor.capture());

        Book capturedBook = bookArgumentCaptor.getValue();

        assertThat(capturedBook).isEqualTo(givenBook);
    }

    @Test
    void testSave() {

        // given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);

        //when
        underTestBookService.save(givenBook, givenAuthor);

        //then
        ArgumentCaptor<Book> bookArgumentCaptor =
                ArgumentCaptor.forClass(Book.class);

        verify(bookRepository)
                .save(bookArgumentCaptor.capture());

        Book capturedBook = bookArgumentCaptor.getValue();

        assertThat(capturedBook).isEqualTo(givenBook);

    }

    @Test
    void testSaveWhenBookByIsbnFoundedAndDuplicateBookFounded() {

        // given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);

        // when
        given(bookRepository.getByIsbn(anyLong())).willReturn(new Book());
        given(bookRepository.getBookByName(anyString())).willReturn(givenBook);


        //then
        assertThat(underTestBookService.save(givenBook, givenAuthor)).isNull();
    }


    @Test
    void CanGetById() throws NotFoundException {
        //given
        long givenBookId = 1L;

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);

        doReturn(Optional.of(givenBook)).when(bookRepository).findById(givenBookId);

        //when
        Book result = underTestBookService.getById(givenBookId);

        //then
        verify(bookRepository).findById(givenBookId);
        assertThat(givenBook).isEqualTo(result);

    }

    @Test
    void CanNotGetById() {

        //given
        long givenBookId = 1L;

        //then
        assertThatThrownBy(() -> underTestBookService.getById(givenBookId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book not found by the id " + givenBookId);
    }


    @Test
    void CanRemoveById() throws NotFoundException {

        //given
        long givenBookId = 1L;

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);


        doReturn(Optional.of(givenBook)).when(bookRepository).findById(givenBookId);

        //when
        underTestBookService.removeById(givenBookId);

        //then
        verify(bookRepository).deleteById(givenBookId);

    }

    @Test
    void CanNotRemoveById() {

        //given
        long givenBookId = 1L;

        //then
        assertThatThrownBy(() -> underTestBookService.removeById(givenBookId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book not found by id " + givenBookId);

    }


    @Test
    void CanUpdate() throws NotFoundException {

        //given
        long givenBookId = 1L;

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);

        when(bookRepository.findById(givenBook.getId()))
                .thenReturn(Optional.of(givenBook));

        ArgumentCaptor<Book> argumentCaptor =
                ArgumentCaptor.forClass(Book.class);

        when(bookRepository.save(argumentCaptor.capture()))
                .thenAnswer(iom -> iom.getArgument(0));


        // When
        givenBook.setName("FakeName");
        Book updated = underTestBookService.update(givenBook);


        // Then
        assertThat(argumentCaptor.getValue().getName())
                .isEqualTo(updated.getName());

    }

    @Test
    void CanNotUpdate() {

        //given
        long givenBookId = 1L;

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);

        //then
        assertThatThrownBy(() -> underTestBookService.update(givenBook))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book not found by the id " + givenBookId);

    }


    @Test
    void getAllByUsedBy() {
        // given
        long givenUsedBy = 1L;

        // when
        underTestBookService.getAllByUsedBy(givenUsedBy);

        // then
        verify(bookRepository).getAllByUsedBy(givenUsedBy);

    }

    @Test
    void testGetAllByUsedBy() {
        // given
        long givenUsedBy = 1L;
        PageRequest pageable = PageRequest.of(3, 5);

        // when
        underTestBookService.getAllByUsedBy(givenUsedBy, pageable);

        // then
        verify(bookRepository).getAllByUsedBy(givenUsedBy, pageable);

    }

    @Test
    void getAllByReservedBy() {
        // given
        long givenReservedBy = 1L;
        PageRequest pageable = PageRequest.of(3, 5);

        // when
        underTestBookService.getAllByReservedBy(givenReservedBy, pageable);

        // then
        verify(bookRepository).getAllByReservedBy(givenReservedBy, pageable);

    }

    @Test
    void getAllByBookStatusAndReservedBy() {

        // given
        long givenReservedBy = 1L;
        BookStatus givenStatus = BookStatus.RESERVED;
        PageRequest pageable = PageRequest.of(3, 5);

        // when
        underTestBookService.getAllByBookStatusAndReservedBy(givenStatus, givenReservedBy, pageable);

        // then
        verify(bookRepository).getBookByBookStatusAndReservedBy(givenStatus, givenReservedBy, pageable);

    }

    @Test
    void getBooksByUserIdAndWithExpirationDate() {

        // given
        long givenUserId = 1L;
        long givenCurrentTime = 21L;
        PageRequest pageable = PageRequest.of(3, 5);

        // when
        underTestBookService.getBooksByUserIdAndWithExpirationDate(givenUserId, givenCurrentTime, pageable);

        // then
        verify(bookRepository).getBooksByUserIdAndWithExpirationDate(givenUserId, givenCurrentTime, pageable);

    }

    @Test
    void testGetAllByBookStatusAndReservedBy() {
        // given
        long givenReservedBy = 1L;
        BookStatus givenStatus = BookStatus.RESERVED;

        // when
        underTestBookService.getAllByBookStatusAndReservedBy(givenStatus, givenReservedBy);

        // then
        verify(bookRepository).getBookByBookStatusAndReservedBy(givenStatus, givenReservedBy);

    }

    @Test
    void doReservation() throws NotFoundException {

        //given
        User givenUser = new User();
        givenUser.setId(1L);
        givenUser.setFirstname("name");
        givenUser.setLastname("lastname");

        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );


        long givenBookId = 1L;

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);

        User employee = new User();
        Collection<Role> roles = new ArrayList<>();
        roles.add(new Role("ROLE_EMPLOYEE"));
        employee.setRoles(roles);

        List<User> employeeList = new ArrayList<>();
        employeeList.add(employee);

        // when
        given(userService.getAll()).willReturn(employeeList);
        given(bookRepository.findById(givenBookId)).willReturn(Optional.of(givenBook));
        underTestBookService.doReservation(givenBook, givenUser);

        // then
        verify(bookRepository).save(givenBook);

    }

    @Test
    void notifyingForPickingBookUp() throws NotFoundException {
        //given
        User givenUser = new User();
        givenUser.setId(1L);
        givenUser.setFirstname("name");
        givenUser.setLastname("lastname");

        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );


        long givenBookId = 1L;

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);
        givenBook.setReservedBy(23546574653524L);

        // when
        given(bookRepository.findById(givenBookId)).willReturn(Optional.of(givenBook));
        underTestBookService.notifyingForPickingBookUp(givenBook, givenUser);

        // then
        verify(bookRepository).save(givenBook);
    }

    @Test
    void notifyingUserForPickingUpBooks() throws NotFoundException {

        //given
        User givenUser = new User();
        givenUser.setId(1L);
        givenUser.setFirstname("name");
        givenUser.setLastname("lastname");

        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );


        long givenBookId = 1L;

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);
        givenBook.setReservedBy(23546574653524L);

        List<Book> books = new ArrayList<>();
        books.add(givenBook);

        // when
        given(bookRepository.findById(givenBookId)).willReturn(Optional.of(givenBook));
        underTestBookService.notifyingUserForPickingUpBooks(books, givenUser);

        // then
        verify(bookRepository).save(givenBook);

    }

    @Test
    @Disabled
    void CanNotSendReservationReadyEmail() {
    }

    @Test
    void sendPickUpAndReceiptEmail() throws NotFoundException, MessagingException {

        // given

        User givenUser = new User();
        givenUser.setId(1L);
        givenUser.setFirstname("name");
        givenUser.setLastname("lastname");


        Receipt receipt = new Receipt();
        receipt.setId(1L);
        receipt.setReceiptStatus(ReceiptStatus.ACTIVE);
        receipt.setBookId(1L);
        receipt.setExpirationDate(2345674563L);
        receipt.setOrderDate(2345678756L);
        receipt.setUserId(1L);
        List<Receipt> receipts = new ArrayList<>();
        receipts.add(receipt);

        long givenBookId = 1L;

        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);
        givenBook.setReservedBy(23546574653524L);
        givenBook.setReturnDate(234567654L);


        given(userService.getById(givenUser.getId())).willReturn(givenUser);
        given(bookRepository.findById(givenBookId)).willReturn(Optional.of(givenBook));
        underTestBookService.sendPickUpAndReceiptEmail(givenUser.getId(), receipts);

    }

    @Test
    void doPickUp() throws NotFoundException {

        // given

        User givenUser = new User();
        givenUser.setId(1L);
        givenUser.setFirstname("name");
        givenUser.setLastname("lastname");
        givenUser.setEmail("FakeEmail");

        long givenBookId = 1L;

        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);
        givenBook.setReservedBy(23546574653524L);
        givenBook.setReturnDate(234567654L);

        List<Book> books = new ArrayList<>();
        books.add(givenBook);

        Receipt receipt = new Receipt(
                1L,
                1234L,
                234536L,
                givenUser.getId(), givenBook.getId(), ReceiptStatus.ACTIVE);


        // when
        given(bookRepository.getBookByBookStatusAndReservedBy(any(BookStatus.class), any(Long.class)))
                .willReturn(books);
        given(bookRepository.findById(givenBookId)).willReturn(Optional.of(givenBook));
        given(receiptService.save(any(Receipt.class))).willReturn(receipt);
        given(userService.getById(givenUser.getId())).willReturn(givenUser);
        underTestBookService.doPickUp(givenUser.getId());
        // then
        verify(bookRepository).save(givenBook);
    }

    @Test
    void doReturnBooks() throws NotFoundException {

        // given

        User givenUser = new User();
        givenUser.setId(1L);
        givenUser.setFirstname("name");
        givenUser.setLastname("lastname");
        givenUser.setEmail("FakeEmail");

        long givenBookId = 1L;

        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        givenBook.setReleaseYear(1234);
        givenBook.setId(givenBookId);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);
        givenBook.setReservedBy(23546574653524L);
        givenBook.setReturnDate(234567654L);
        givenBook.setUsedBy(givenUser.getId());

        Set<Book> books = new HashSet<>();
        books.add(givenBook);

        Receipt receipt = new Receipt(
                1L,
                1234L,
                234536L,
                givenUser.getId(), givenBook.getId(), ReceiptStatus.ACTIVE
        );

        // when
        given(bookRepository.findById(givenBookId)).willReturn(Optional.of(givenBook));
        given(receiptService.getReceiptByBookIdAndUserIdAndReceiptStatus(
                givenBook.getId(), givenBook.getUsedBy(), ReceiptStatus.ACTIVE))
                .willReturn(receipt);
        given(userService.getById(givenUser.getId())).willReturn(givenUser);
        underTestBookService.doReturnBooks(books, givenUser.getId());

        // then
        verify(receiptService).update(any(Receipt.class));
        verify(notificationService).save(any(Notification.class));
        verify(userService).getById(any(Long.class));
    }

    @Test
    @Disabled
    void sendEmailAboutReturnedBook() {
    }

    @Test
    void findPaginated() {
        // given
        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        givenBook.setReleaseYear(1234);
        givenBook.setId(1L);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);
        givenBook.setReservedBy(23546574653524L);
        givenBook.setReturnDate(234567654L);
        givenBook.setUsedBy(1L);

        Book givenBook1 = new Book();
        givenBook1.setName("book1");
        givenBook1.setGenre(BookGenre.CONTEMPORARY);
        givenBook1.setBookStatus(BookStatus.CHECKED_IN);
        givenBook1.setReleaseYear(1234);
        givenBook1.setId(2L);
        givenBook1.setAuthors(authorList);
        givenBook1.setReservedBy(23546574653524L);
        givenBook1.setReturnDate(234567654L);
        givenBook1.setUsedBy(1L);

        Collection<Book> books = new ArrayList<>();
        books.add(givenBook);
        books.add(givenBook1);

        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("id"));

        // when
        underTestBookService.findPaginated(pageRequest, books);

        //then
        assertThat(underTestBookService.findPaginated(pageRequest, books)).isNotNull();
    }

    @Test
    void testFindPaginated() {
        // given

        User givenUser = new User();
        givenUser.setId(1L);
        givenUser.setFirstname("name");
        givenUser.setLastname("lastname");
        givenUser.setEmail("FakeEmail");

        User givenUser1 = new User();
        givenUser1.setId(2L);
        givenUser1.setFirstname("name");
        givenUser1.setLastname("lastname");
        givenUser1.setEmail("FakeEmail");

        Author givenAuthor = new Author(
                "Agatha",
                "Christie",
                Gender.FEMALE
        );

        Book givenBook = new Book();
        givenBook.setName("book1");
        givenBook.setGenre(BookGenre.CONTEMPORARY);
        givenBook.setBookStatus(BookStatus.CHECKED_IN);
        givenBook.setReleaseYear(1234);
        givenBook.setId(1L);
        List<Author> authorList = new ArrayList<>();
        authorList.add(givenAuthor);
        givenBook.setAuthors(authorList);
        givenBook.setReservedBy(23546574653524L);
        givenBook.setReturnDate(234567654L);
        givenBook.setUsedBy(1L);

        Book givenBook1 = new Book();
        givenBook1.setName("book1");
        givenBook1.setGenre(BookGenre.CONTEMPORARY);
        givenBook1.setBookStatus(BookStatus.CHECKED_IN);
        givenBook1.setReleaseYear(1234);
        givenBook1.setId(2L);
        givenBook1.setAuthors(authorList);
        givenBook1.setReservedBy(23546574653524L);
        givenBook1.setReturnDate(234567654L);
        givenBook1.setUsedBy(1L);

        Map<Book, User> books = new HashMap<>();
        books.put(givenBook, givenUser);
        books.put(givenBook1, givenUser1);

        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("id"));

        // when
        underTestBookService.findPaginated(pageRequest, books);

        //then
        assertThat(underTestBookService.findPaginated(pageRequest, books)).isNotNull();

    }
}