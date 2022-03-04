package com.app.library.util;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Book;
import com.app.library.model.ListEntry;
import com.app.library.model.User;
import com.app.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class Util {

    private final BookService bookService;

    public Util(BookService bookService) {
        this.bookService = bookService;
    }


    public void selectedBooks (
            String selectedBookIdsInString,
            Long selectedBookId,
            Long userId,
            Set<Book> selectedBooks,
            List<Book> booksInUseByUser) throws NotFoundException {

        if (selectedBookIdsInString != null && !selectedBookIdsInString.isEmpty()
                && selectedBookId != null && userId != null && selectedBookId != 0 && userId != 0) {

            List<String> strings = Arrays.asList(selectedBookIdsInString.split(","));
            List<Long> bookIds = strings.stream().map(Long::parseLong).collect(Collectors.toList());

            for (Long id : bookIds) {
                selectedBooks.add(bookService.getById(id));
                booksInUseByUser.removeIf(book -> book.getId().equals(id));
            }
        }
    }

    public String removeSelectedBooks (
            Long removeBookId,
            String selectedBookIdsInString,
            Long userId,
            Set<Book> selectedBooks,
            List<Book> booksInUseByUser) throws NotFoundException {
        if (removeBookId != null && removeBookId != 0 && selectedBookIdsInString != null
                && userId != null && userId != 0 && !selectedBookIdsInString.isEmpty()) {

            List<String> strings = Arrays.asList(selectedBookIdsInString.split(","));
            List<Long> bookIds = strings.stream().map(Long::parseLong).collect(Collectors.toList());

            bookIds.remove(removeBookId);
            strings = bookIds.stream().map(String::valueOf).collect(Collectors.toList());
            selectedBookIdsInString = String.join(",", strings);

            if (!selectedBookIdsInString.isEmpty() && !selectedBookIdsInString.endsWith(","))
                selectedBookIdsInString += ",";
            selectedBooks.clear();

            for (Long id : bookIds) {
                selectedBooks.add(bookService.getById(id));
                booksInUseByUser.removeIf(book -> book.getId().equals(id));
            }
        }
        return selectedBookIdsInString;
    }

    public void returnBooks(Set<Book> selectedBooks, String selectedBookIdsInString, Long userId) throws NotFoundException {
        if (selectedBookIdsInString != null && !selectedBookIdsInString.isEmpty() && userId != null) {

            List<String> strings = Arrays.asList(selectedBookIdsInString.split(","));
            List<Long> bookIds = strings.stream().map(Long::parseLong).collect(Collectors.toList());

            for (Long id : bookIds) {
                selectedBooks.add(bookService.getById(id));
            }
        }
    }

    public List<Integer> pageNumbers(Page<?> page) {
        List<Integer> pageNumbers = new ArrayList<>();
        int totalPages = page.getTotalPages();
        if (totalPages > 0) {
             pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
        }
        return pageNumbers;
    }

    public List<Integer> pageNumbersMap(Page<ListEntry<Book, User>> page) {
        List<Integer> pageNumbers = new ArrayList<>();
        int totalPages = page.getTotalPages();
        if (totalPages > 0) {
            pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
        }
        return pageNumbers;
    }

}
