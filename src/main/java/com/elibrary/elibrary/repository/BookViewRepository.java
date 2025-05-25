package com.elibrary.elibrary.repository;

import com.elibrary.elibrary.model.BookView;
import com.elibrary.elibrary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookViewRepository extends JpaRepository<BookView, Long> {
    List<BookView> findByUserAndViewedAtAfter(User user, LocalDateTime after);
}