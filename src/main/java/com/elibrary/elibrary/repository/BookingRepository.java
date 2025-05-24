package com.elibrary.elibrary.repository;

import com.elibrary.elibrary.model.Booking;
import com.elibrary.elibrary.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>{
    List<Booking> findByUserId(Long userId);
    List<Booking> findByStatus(BookingStatus status);
}
