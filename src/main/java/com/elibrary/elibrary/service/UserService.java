package com.elibrary.elibrary.service;

import com.elibrary.elibrary.dto.UserWithBookingsDto;
import com.elibrary.elibrary.dto.BookingDto;
import com.elibrary.elibrary.model.Booking;
import com.elibrary.elibrary.model.User;
import com.elibrary.elibrary.repository.UserRepository;
import com.elibrary.elibrary.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Autowired
    public UserService(UserRepository userRepository, BookingRepository bookingRepository, BookingService bookingService) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService=bookingService;
    }

    public List<Map<String, Object>> getAllUsersWithBookings() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            List<Booking> bookings = bookingService.getBookingsByUser(user.getId());

            List<BookingDto> bookingDtos = bookings.stream()
                    .map(b -> new BookingDto(
                            b.getId(),
                            b.getBook().getTitle(),
                            b.getBookingDate(),
                            b.getStatus().name()
                    ))
                    .collect(Collectors.toList());

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getUsername());
            userMap.put("email", user.getUsername()); // если email == username
            userMap.put("roles", List.of(user.getRole().name()));
            userMap.put("reservations", bookingDtos);

            result.add(userMap);
        }

        return result;
    }
}