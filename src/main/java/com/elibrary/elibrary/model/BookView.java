package com.elibrary.elibrary.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_views")
public class BookView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Book book;

    private LocalDateTime viewedAt;

    public BookView() {}

    public BookView(User user, Book book, LocalDateTime viewedAt) {
        this.user = user;
        this.book = book;
        this.viewedAt = viewedAt;
    }

    public Long getId() { return id; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }

    public void setBook(Book book) { this.book = book; }

    public LocalDateTime getViewedAt() { return viewedAt; }

    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}