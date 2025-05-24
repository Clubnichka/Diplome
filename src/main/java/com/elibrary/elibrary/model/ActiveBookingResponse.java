package com.elibrary.elibrary.model;

import java.time.LocalDate;

public class ActiveBookingResponse {
    private Long id;
    private String bookTitle;
    private String userName;
    private LocalDate expiryDate;

    public ActiveBookingResponse(Long id, String bookTitle, String userName, LocalDate expiryDate) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.userName = userName;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getUserName() {
        return userName;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}