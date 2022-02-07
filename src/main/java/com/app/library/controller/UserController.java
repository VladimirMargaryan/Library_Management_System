package com.app.library.controller;

import com.app.library.exception.*;
import com.app.library.model.*;
import com.app.library.service.BookService;
import com.app.library.service.NotificationService;
import com.app.library.service.UserService;
import com.app.library.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private Util util;

	@GetMapping
	public String userHome(@RequestParam (required = false) Optional<Integer> pageOfBooks,
						   Model model) {

        User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		Page<Book> bookPage = bookService.getBooksByUserIdAndWithExpirationDate(
				user.getId(),
				System.currentTimeMillis(),
				PageRequest.of(pageOfBooks.orElse(1) - 1, 5));

		model.addAttribute("bookPage", bookPage);
		model.addAttribute("bookPageNumbers", util.pageNumbers(bookPage));
		model.addAttribute("notifications", notifications);
		model.addAttribute("user", user);
		return "user/user-home";
	}

    @GetMapping(value="/yourbooks")
	public String yourBooks(@RequestParam (required = false) Optional<Integer> page,
							Model model) {

		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		Page<Book> bookPage = bookService.getAllByUsedBy(user.getId(), PageRequest.of(page.orElse(1) - 1, 5));
		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("bookPage", bookPage);
		model.addAttribute("pageNumbers", util.pageNumbers(bookPage));
		return "user/user-your-books";
	}

    @GetMapping(value="/browsebooks")
	public String browseBooks(@RequestParam (required = false) String keyword,
							  @RequestParam (required = false) String sortBy,
							  @RequestParam (required = false) Long  reservedBookId,
							  @RequestParam (required = false) Optional<Integer> page,
							  Model model) throws NotFoundException {


		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		Page<Book> bookPage;

		if (reservedBookId != null) {
			Book book = bookService.getById(reservedBookId);
			bookService.doReservation(book, user);
		}

		if (sortBy == null || sortBy.isEmpty())
			sortBy = "id";

		Pageable bookPagePageable = PageRequest.of(page.orElse(1) - 1, 5, Sort.by(sortBy));
		if (keyword != null && !keyword.isEmpty())
			bookPage = bookService.search(keyword, bookPagePageable);
		else
			bookPage = bookService.getAll(bookPagePageable);

		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("bookPage", bookPage);
		model.addAttribute("pageNumbers", util.pageNumbers(bookPage));
		model.addAttribute("keyword", keyword);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("reservedBookId", reservedBookId);
		model.addAttribute("user", user);
		return "user/user-browse-books";
	}

	@PutMapping(value="/yourbooks/extend")
	public String extendRequest(@RequestParam Long bookId) throws NotFoundException {

		Book book = bookService.getById(bookId);

		if (book.getReservedBy() == null) {
			book.setReturnDate(book.getReturnDate() + 1000 * 60 * 60 * 24 * 15);
			bookService.update(book);
			return "redirect:/user/yourbooks/bookextended";
		} else
			return "redirect:/user/yourbooks/bookcannotbeextended";
	}

	@GetMapping(value="/yourbooks/bookextended")
	public String bookExtended() {
		return "user/user-book-extended";
	}

	@GetMapping(value="/yourbooks/bookcannotbeextended")
	public String bookCanNotBeExtended() {
		return "user/user-book-can-not-be-extended";
	}



	@GetMapping(value="/FAQ")
	public String FAQ(Model model) {
		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		return "user/user-FAQ";
	}


	@PutMapping(value="/browsebooks/payreservation")
	public String payReservation(@RequestParam String reservedBookIdsInString,
								 Model model) {

		model.addAttribute("reservedBookIdsInString", reservedBookIdsInString);
		return "user/user-pay-reservation";
	}

	@GetMapping(value="/yourreservations")
	public String yourReservations(@RequestParam (required = false) Optional<Integer> page,
								   @RequestParam (required = false) String sortBy,
								   Model model) {
		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

		if (sortBy == null || sortBy.isEmpty())
			sortBy = "id";

		Pageable bookPageable = PageRequest.of(page.orElse(1) - 1, 5, Sort.by(sortBy));

		Page<Book> bookPage = bookService.getAllByReservedBy(user.getId(), bookPageable);

		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("bookPage", bookPage);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("pageNumbers", util.pageNumbers(bookPage));
		return "user/user-your-reservations";
	}
}
