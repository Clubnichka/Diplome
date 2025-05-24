// package com.elibrary.elibrary.dto;

package com.elibrary.elibrary.dto;

import java.time.LocalDate;

public class BookingDto {
    private Long id;
    private String bookTitle;
    private LocalDate bookingDate;
    private String status;

    public BookingDto() {
    }

    public BookingDto(Long id, String bookTitle, LocalDate bookingDate, String status) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}