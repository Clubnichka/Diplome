package com.elibrary.elibrary.service;

import com.elibrary.elibrary.model.Book;
import com.elibrary.elibrary.model.Booking;
import com.elibrary.elibrary.model.BookingStatus;
import com.elibrary.elibrary.model.User;
import com.elibrary.elibrary.repository.BookRepository;
import com.elibrary.elibrary.repository.BookingRepository;
import com.elibrary.elibrary.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(Long bookId, Long userId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (bookOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("Книга или пользователь не найдены.");
        }

        Book book = bookOpt.get();
        User user = userOpt.get();

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Нет доступных экземпляров книги.");
        }

        // Уменьшаем количество доступных экземпляров
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Booking booking = new Booking();
        booking.setBook(book);
        booking.setUser(user);
        booking.setBookingDate(LocalDate.now());
        booking.setStatus(BookingStatus.ACTIVE);

        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public boolean isBookAvailable(Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> book.getAvailableCopies() > 0)
                .orElse(false);
    }

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }
}