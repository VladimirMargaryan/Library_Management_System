package com.app.library.controller;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.app.library.util.Util;
import com.app.library.exception.NotFoundException;
import com.app.library.model.*;
import com.app.library.service.AuthorService;
import com.app.library.service.BookService;
import com.app.library.service.NotificationService;
import com.app.library.service.UserService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping(value="/employee")
public class EmployeeController {

	private final UserService userService;

	private final BookService bookService;

	private final AuthorService authorService;

	private final NotificationService notificationService;

	private final Util util;

	public EmployeeController(UserService userService,
							  BookService bookService,
							  AuthorService authorService,
							  NotificationService notificationService,
							  Util util) {

		this.userService = userService;
		this.bookService = bookService;
		this.authorService = authorService;
		this.notificationService = notificationService;
		this.util = util;
	}


	@GetMapping
	public String employeeHomePage(Model model) {

		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("user", user);
		return"employee/employee-home";
	}

	@GetMapping(value="/users/showusers")
	public String showUsers(Model model,
							@RequestParam (required = false)String keyword,
							@RequestParam (required = false)String sortBy,
							@RequestParam (required = false) Optional<Integer> page) {

		Page<User> userPage;

		if (sortBy == null || sortBy.isEmpty())
			sortBy = "id";

		Pageable userPagePageable = PageRequest.of(page.orElse(1) - 1, 10, Sort.by(sortBy));
		if (keyword != null && !keyword.isEmpty())
			userPage = userService.search(keyword, userPagePageable);
		else
			userPage = userService.getAll(userPagePageable);

		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("pageNumbers", util.pageNumbers(userPage));
		model.addAttribute("userPage", userPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("sortBy", sortBy);
 		return "employee/employee-show-users";
	}

	@GetMapping(value="/users/showuserinfo/{userId}")
	public String showUserInfo(@PathVariable Long userId,
							   @RequestParam (required = false) Optional<Integer> usedBooksPage,
							   @RequestParam (required = false) Optional<Integer> reservedBooksPage,
							   Model model) throws NotFoundException {
		User user = userService.getById(userId);
		Page<Book> bookPage  = bookService.getAllByUsedBy(user.getId(), PageRequest.of(usedBooksPage.orElse(1) - 1, 5));

		Page<Book> reservedBookPage = bookService.getAllByReservedBy(user.getId(), PageRequest.of(reservedBooksPage.orElse(1) - 1, 5));

		User userNotify = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(userNotify.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("usedBooksPageNumbers", util.pageNumbers(bookPage));
		model.addAttribute("reservedBooksPageNumbers", util.pageNumbers(reservedBookPage));
		model.addAttribute("bookPage", bookPage);
		model.addAttribute("reservedBookPage", reservedBookPage);
		model.addAttribute("userId", userId);
		model.addAttribute("user", user);
		return "employee/employee-show-user-info";
	}

	@GetMapping(value="/books")
	public String books() {
		return "redirect:/employee/books/showbooks";
	}

	@GetMapping(value="/books/showbooks")
	public String showBooks(Model model,
							@RequestParam (required=false) String keyword,
							@RequestParam (required=false) String sortBy,
							@RequestParam (required = false) Optional<Integer> page) throws NotFoundException {

		Page<Book> bookPage;
		Map<Long, User> booksUsers = new LinkedHashMap<>();

		if (sortBy == null || sortBy.isEmpty())
			sortBy = "id";

		Pageable bookPageable = PageRequest.of(page.orElse(1) - 1, 5, Sort.by(sortBy));

		if (keyword != null && !keyword.isEmpty())
			bookPage = bookService.search(keyword, bookPageable);
		else
			bookPage = bookService.getAll(bookPageable);

		for (Book book : bookPage) {
			if (book.getUsedBy() != null)
				booksUsers.put(book.getUsedBy(), userService.getById(book.getUsedBy()));
		}

		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("pageNumbers", util.pageNumbers(bookPage));
		model.addAttribute("bookPage", bookPage);
		model.addAttribute("booksUsers", booksUsers);
		model.addAttribute("keyword", keyword);
		model.addAttribute("page", page);
		model.addAttribute("sortBy", sortBy);
		return "employee/employee-show-books";
	}

	@GetMapping(value="/books/newbook")
	public String newBook(Model model) {
		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("book", new Book());
		model.addAttribute("author", new Author());
		return "employee/employee-new-book";
	}

	@PostMapping(value="/books/save")
	public String saveBook(Book book,
						   Author author,
						   @RequestParam("file") MultipartFile file,
						   Model model) {

		if (file != null && !file.isEmpty()) {
			try {
				book.setPhoto(Base64.encodeBase64String(file.getBytes()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Author bookAuthor = authorService.save(author);
		Book book1 = bookService.save(book, bookAuthor);
		if (book1 == null){
			model.addAttribute("title", "Book Exists!");
			model.addAttribute("message", "Book already exists in our database!");
			return "book-already-exists";
		} else
			return "redirect:/employee/books/booksaved";
	}

	@GetMapping(value="/books/booksaved")
	public String bookSaved() {
		return "employee/employee-book-saved";
	}

	@GetMapping(value="/books/areyousuretodeletebook/{deleteBookId}")
	public String areYouSureToDeleteBook(@PathVariable Long deleteBookId,
										 Model model) throws NotFoundException {
		Book book = bookService.getById(deleteBookId);
		model.addAttribute("deleteBook", book);
		return "employee/employee-delete-book";
	}

	@DeleteMapping(value="/books/deletebook/{deleteBookId}")
	public String deleteBook(@PathVariable Long deleteBookId) throws NotFoundException {
		bookService.removeById(deleteBookId);
		return "redirect:/employee/books/bookdeleted";
	}

	@GetMapping(value="/books/bookdeleted")
	public String bookDeleted() {
		return "employee/employee-book-deleted";
	}

	@GetMapping(value="/books/changebookinfo/{changeBookId}")
	public String changeBookInfo(@PathVariable Long changeBookId,
								 Model model) throws NotFoundException {
		Book book = bookService.getById(changeBookId);
		Author author = authorService.getByBookId(changeBookId);
		User currentUser = null;
		User reservedUser = null;
		if (book.getUsedBy() != null)
			currentUser = userService.getById(book.getUsedBy());
		if (book.getReservedBy() != null)
			reservedUser = userService.getById(book.getReservedBy());

		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("book", book);
		model.addAttribute("author", author);
		model.addAttribute("currentUser", currentUser);
		model.addAttribute("reservedUser", reservedUser);
		return "employee/employee-change-book-info";
	}

	@PutMapping(value="/books/savebookchange")
	public String updateBookInfo(Author author,
								 @RequestParam(value = "file", required = false) MultipartFile file,
								 Book book) throws NotFoundException {
		Author bookAuthor = authorService.save(author);
		List<Author> authors = new ArrayList<>();
		authors.add(bookAuthor);
		book.setAuthors(authors);

		if (file != null && !file.isEmpty()) {
			try {
				book.setPhoto(Base64.encodeBase64String(file.getBytes()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		bookService.update(book);
		return "redirect:/employee/books/bookinfochanged";
	}

	@GetMapping(value="/books/bookinfochanged")
	public String bookInfoChanged() {
		return "employee/employee-book-information-changed";
	}

	@GetMapping(value="/orders")
	public String orders(@RequestParam (required = false) String keyword,
						 @RequestParam (required = false) String sortBy,
						 @RequestParam (required = false) Long userId,
						 @RequestParam (required = false) String bookKeyword,
						 @RequestParam (required = false) Long selectedBookId,
						 @RequestParam (required = false) Long removedBookId,
						 @RequestParam (required = false) Optional<Integer> page,
						 @RequestParam (required = false) Optional<Integer> selectedBookPage,
						 @RequestParam (required = false) Optional<Integer> browseBookPage,
						 Model model) throws NotFoundException {

		Page<User> userPage;

		Pageable userPagePageable = PageRequest.of(page.orElse(1) - 1, 5, Sort.by("id"));
		if (keyword != null && !keyword.isEmpty())
			userPage = userService.search(keyword, userPagePageable);
		else
			userPage = userService.getAll(userPagePageable);

		User user = null;
		if (userId != null && userId != 0)
			user = userService.getById(userId);


		if (selectedBookId != null && selectedBookId != 0){
			Book book = bookService.getById(selectedBookId);
			book.setBookStatus(BookStatus.READY_FOR_PICK_UP);
			book.setReservedBy(userId);
			book.setReservedUntil(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7);
			bookService.update(book);
		}

		if (removedBookId != null && removedBookId != 0){
			Book book = bookService.getById(removedBookId);
			book.setBookStatus(BookStatus.CHECKED_IN);
			book.setReservedUntil(null);
			book.setReservedBy(null);
			bookService.update(book);
		}

		if (sortBy == null || sortBy.isEmpty())
			sortBy = "id";

		Page<Book> searchedBookPage;
		Pageable searchedBooksPageable = PageRequest.of(browseBookPage.orElse(1) - 1, 5, Sort.by(sortBy));

		if (bookKeyword != null && !bookKeyword.isEmpty())
			searchedBookPage = bookService.search(bookKeyword, searchedBooksPageable);
		else
			searchedBookPage = bookService.getAll(searchedBooksPageable);

		Pageable bookPagePageable = PageRequest.of(selectedBookPage.orElse(1) - 1, 5, Sort.by(sortBy));
		Page<Book> bookPage = bookService.getAllByBookStatusAndReservedBy(BookStatus.READY_FOR_PICK_UP, userId, bookPagePageable);


		User userNotify = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(userNotify.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("pageNumbers", util.pageNumbers(userPage));
		model.addAttribute("bookPageNumbers", util.pageNumbers(bookPage));
		model.addAttribute("searchedBookPageNumbers", util.pageNumbers(searchedBookPage));
		model.addAttribute("userPage", userPage);
		model.addAttribute("bookPage", bookPage);
		model.addAttribute("searchedBookPage", searchedBookPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("bookKeyword", bookKeyword);
		model.addAttribute("selectedBookId", selectedBookId);
		model.addAttribute("removedBookId", removedBookId);
		model.addAttribute("userId", userId);
		model.addAttribute("user", user);

		return "employee/employee-orders";
	}

	@GetMapping(value="/confirmorder")
	public String confirmOrder(@RequestParam (required = false) Optional<Integer> page,
							   @RequestParam (required = false) Optional<Integer> selectedBookPage,
							   @RequestParam (required = false) Optional<Integer> browseBookPage,
							   @RequestParam Long userId,
							   Model model) throws NotFoundException {

		Page<Book> bookPage = bookService.getAllByBookStatusAndReservedBy(
				BookStatus.READY_FOR_PICK_UP, userId,
				PageRequest.of(page.orElse(1) - 1, 5));

		User user = userService.getById(userId);

		model.addAttribute("user", user);
		model.addAttribute("bookPage", bookPage);
		model.addAttribute("bookPageNumbers", util.pageNumbers(bookPage));
		model.addAttribute("selectedBookPage", selectedBookPage);
		model.addAttribute("browseBookPage", browseBookPage);

		return "employee/employee-confirm-order";
	}

	@PutMapping(value="/saveorder")
	public String saveOrder(@RequestParam Long userId) throws NotFoundException {
		bookService.doPickUp(userId);
		return "redirect:/employee/ordersaved";
	}

	@GetMapping(value="/ordersaved")
	public String orderSaved() {
		return "employee/employee-order-saved";
	}


	@GetMapping(value="/returnedbooks")
	public String returnedBooks(@RequestParam (required = false) Long userId,
								@RequestParam (required = false) String keyword,
								@RequestParam (required = false) Long selectedBookId,
								@RequestParam (required = false) Long removeBookId,
								@RequestParam (required = false) String selectedBookIdsInString,
								@RequestParam (required = false) Optional<Integer> page,
								@RequestParam (required = false) Optional<Integer> selectedBookPage,
								@RequestParam (required = false) Optional<Integer> inUseBookPage,
								Model model) throws NotFoundException {

		Page<User> userPage;

		Pageable userPagePageable = PageRequest.of(page.orElse(1) - 1, 5, Sort.by("id"));

		if (keyword != null && !keyword.isEmpty())
			userPage = userService.search(keyword, userPagePageable);
		else
			userPage = userService.getAll(userPagePageable);

		User user = null;
		List<Book> booksInUseByUser = new ArrayList<>();
		Set<Book> selectedBooks = new LinkedHashSet<>();
		if (userId != null && userId != 0) {
			user = userService.getById(userId);
			booksInUseByUser = bookService.getAllByUsedBy(user.getId());
		}

		if (selectedBookId != null && selectedBookId != 0) {
			selectedBookIdsInString += selectedBookId + ",";
		}

			util.selectedBooks(selectedBookIdsInString, selectedBookId, userId, selectedBooks, booksInUseByUser);

		selectedBookIdsInString =
				util.removeSelectedBooks(removeBookId, selectedBookIdsInString, userId, selectedBooks, booksInUseByUser);


		int currentBooksPage = inUseBookPage.orElse(1);
		Page<Book> booksOfUSer = bookService.findPaginated(PageRequest.of(currentBooksPage - 1, 5), booksInUseByUser);

		int currentSelectedBooksPage = selectedBookPage.orElse(1);
		Page<Book> selectedBooksOfUser = bookService.findPaginated(PageRequest.of(currentSelectedBooksPage - 1, 5), selectedBooks);

		if (booksOfUSer.getContent().isEmpty() && currentBooksPage - 2 >= 0)
			booksOfUSer = bookService.findPaginated(PageRequest.of(currentBooksPage - 2, 5), booksInUseByUser);

		if (selectedBooksOfUser.getContent().isEmpty() && currentSelectedBooksPage - 2 >= 0)
			selectedBooksOfUser = bookService.findPaginated(PageRequest.of(currentSelectedBooksPage - 2, 5), selectedBooks);

		User userNotify = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(userNotify.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("selectedBookIdsInString", selectedBookIdsInString);
		model.addAttribute("userPage", userPage);
		model.addAttribute("userPageNumbers", util.pageNumbers(userPage));
		model.addAttribute("booksOfUSer", booksOfUSer);
		model.addAttribute("booksOfUSerPageNumbers", util.pageNumbers(booksOfUSer));
		model.addAttribute("selectedBooksOfUser", selectedBooksOfUser);
		model.addAttribute("selectedBooksOfUserPageNumbers", util.pageNumbers(selectedBooksOfUser));
		model.addAttribute("user", user);
		model.addAttribute("keyword", keyword);
		model.addAttribute("userId", userId);
		model.addAttribute("selectedBookId", selectedBookId);
		model.addAttribute("removeBookId", removeBookId);
		return "employee/employee-returned-books";
	}

	@GetMapping(value="/confirmreturnedbooks")
	public String confirmReturnedBooks(@RequestParam Long userId,
									   @RequestParam String selectedBookIdsInString,
									   Model model) throws NotFoundException {

		Set<Book> selectedBooks = new LinkedHashSet<>();
		util.returnBooks(selectedBooks, selectedBookIdsInString, userId);

		model.addAttribute("selectedBooks", selectedBooks);
		model.addAttribute("selectedBookIds", selectedBookIdsInString);
		model.addAttribute("user", userService.getById(userId));
		return "employee/employee-confirm-returned-books";
	}


	@PutMapping(value="/savereturnedbooks")
	public String saveReturnedBooks(@RequestParam Long userId,
									@RequestParam String selectedBookIdsInString) throws NotFoundException {

		Set<Book> selectedBooks = new LinkedHashSet<>();
		util.returnBooks(selectedBooks, selectedBookIdsInString, userId);
		bookService.doReturnBooks(selectedBooks, userId);
		return "redirect:/employee/booksreturned";
	}

	@GetMapping(value="/booksreturned")
	public String booksReturned() {
		return "employee/employee-books-returned";
	}

	@GetMapping(value="/reservations")
	public String reservations(@RequestParam(required = false) Optional<Integer> unprocessedPage,
							   @RequestParam(required = false) Optional<Integer> processedPage,
							   Model model) throws NotFoundException {
		Map<Book, User> usersAndUnprocessedReservations = new LinkedHashMap<>();
		Map<Book, User> usersAndProcessedReservations = new LinkedHashMap<>();

		List<Book> unprocessedReservations = bookService.getAllByStatus(BookStatus.RESERVED);
		List<Book> processedReservations = bookService.getAllByStatus(BookStatus.READY_FOR_PICK_UP);

		for (Book book : unprocessedReservations)
			usersAndUnprocessedReservations.put(book, userService.getById(book.getReservedBy()));

		for (Book book : processedReservations)
			usersAndProcessedReservations.put(book, userService.getById(book.getReservedBy()));

		int currentUnprocessedPage = unprocessedPage.orElse(1);
		Page<ListEntry<Book, User>> booksOfUSer = bookService.findPaginated(PageRequest.of(currentUnprocessedPage - 1, 5), usersAndUnprocessedReservations);

		int currentProcessedPage = processedPage.orElse(1);
		Page<ListEntry<Book, User>> selectedBooksOfUser = bookService.findPaginated(PageRequest.of(currentProcessedPage - 1, 5), usersAndProcessedReservations);

		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("unprocessedReservations", unprocessedReservations);
		model.addAttribute("booksOfUSer", booksOfUSer);
		model.addAttribute("booksOfUSerPageNumbers", util.pageNumbersMap(booksOfUSer));
		model.addAttribute("selectedBooksOfUser", selectedBooksOfUser);
		model.addAttribute("selectedBooksOfUserPageNumbers", util.pageNumbersMap(selectedBooksOfUser));
		model.addAttribute("form", new ListForm(unprocessedReservations));
		return "employee/employee-reservations";
	}

	@PutMapping(value="/setreadyforpickup")
	public String setReadyForPickup(@RequestParam (required = false) Optional<Integer> unprocessedPage,
									@RequestParam (required = false) Optional<Integer> processedPage,
									@RequestParam Long bookId,
									@RequestParam Long userId,
									Model model) throws NotFoundException {
		model.addAttribute("user", userService.getById(userId));
		model.addAttribute("book", bookService.getById(bookId));
		model.addAttribute("unprocessedPage", unprocessedPage);
		model.addAttribute("processedPage", processedPage);
		return "employee/employee-reservation-ready-for-pick-up";
	}

	@PutMapping(value="/setreadyforpickupAll")
	public String setReadyForPickupAll(@RequestParam (required = false) Optional<Integer> unprocessedPage,
									   @RequestParam (required = false) Optional<Integer> processedPage,
									   ListForm form,
									   Model model) throws NotFoundException {

		List<ListEntry<Book, User>> booksOfUSer = new ArrayList<>();
		for (Book book : form.getBooks())
			booksOfUSer.add(new ListEntry<>(book, userService.getById(book.getReservedBy())));

		model.addAttribute("booksOfUSer", booksOfUSer);
		model.addAttribute("unprocessedPage", unprocessedPage);
		model.addAttribute("processedPage", processedPage);
		model.addAttribute("form", form);
		return "employee/employee-all-reservations-ready-for-pick-up";
	}


	@PutMapping(value="/updatebookreservation")
	public String updateBookReservation(@RequestParam(required = false) Optional<Integer> unprocessedPage,
										@RequestParam(required = false) Optional<Integer> processedPage,
										@RequestParam Long bookId,
										@RequestParam Long userId,
										Model model) throws NotFoundException {

		Book book = bookService.getById(bookId);
		User user = userService.getById(userId);
		bookService.notifyingForPickingBookUp(book, user);

		model.addAttribute("unprocessedPage", unprocessedPage);
		model.addAttribute("processedPage", processedPage);
		return "redirect:/employee/reservations";
	}

	@PutMapping(value="/updatebooksreservations")
	public String updateBooksReservations(@RequestParam(required = false) Optional<Integer> unprocessedPage,
										  @RequestParam(required = false) Optional<Integer> processedPage,
										  ListForm form,
										  Model model) throws NotFoundException {
		while (!form.getBooks().isEmpty()){
			long reservedBy = form.getBooks().get(0).getReservedBy();
			List<Book> oneUserBooks = form.getBooks()
					.stream()
					.filter(book -> book.getReservedBy().equals(reservedBy))
					.collect(Collectors.toList());
			form.getBooks().removeIf(book -> book.getReservedBy().equals(reservedBy));
			bookService.notifyingUserForPickingUpBooks(oneUserBooks, userService.getById(reservedBy));
		}

		model.addAttribute("unprocessedPage", unprocessedPage);
		model.addAttribute("processedPage", processedPage);
		return "redirect:/employee/reservations";
	}

}
