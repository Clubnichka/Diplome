package com.elibrary.elibrary.controller;

import com.elibrary.elibrary.model.Book;
import com.elibrary.elibrary.model.Booking;
import com.elibrary.elibrary.model.BookingStatus;
import com.elibrary.elibrary.model.User;
import com.elibrary.elibrary.security.CustomUserDetails;
import com.elibrary.elibrary.service.AuthService;
import com.elibrary.elibrary.service.BookService;
import com.elibrary.elibrary.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        System.out.println("id пользователя"+user.getId());
        Optional<Book> optionalBook = bookService.getBookById(request.getBookId());

        if (optionalBook.isEmpty()) {
            return ResponseEntity.badRequest().body("Книга не найдена");
        }

        Book book = optionalBook.get();

        if (book.getAvailableCopies() <= 0) {
            return ResponseEntity.badRequest().body("Нет доступных экземпляров");
        }

        // Уменьшаем количество доступных экземпляров
        bookService.decrementAvailableCopies(book.getId());

        // Создаём бронирование
        Booking booking = new Booking();
        booking.setBook(book);
        booking.setUser(user);
        booking.setBookingDate(LocalDate.now());
        booking.setStatus(BookingStatus.ACTIVE);  // или BookingStatus.BOOKED, если нужно

        bookingService.save(booking);

        return ResponseEntity.ok("Бронирование успешно создано");
    }

    public static class BookingRequest {
        private Long bookId;
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
    }
}