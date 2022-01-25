package com.app.library;

import com.app.library.controller.UserController;
import com.app.library.model.Book;
import com.app.library.model.User;
import com.app.library.service.BookService;
import com.app.library.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;


@SpringBootApplication
@EnableAsync
public class LibraryApplication {
	public static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);

//		ConfigurableApplicationContext context = SpringApplication.run(LibraryApplication.class, args);
//        UserController controller = (UserController) context.getBean("userController") ;
//        UserService userService = (UserService) context.getBean("userServiceImpl");
//        User user = userService.getByEmail("valodyamargaryan549@gmail.com");
//		userService.sendEmailAboutVerificationOfAccount(user);
//		BookService bookService = (BookService) context.getBean("bookServiceImpl");
//		List<Book> books = bookService.getAll();
//		books.forEach(System.out::println);

	}

}
