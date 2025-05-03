package com.elibrary.elibrary.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author;

    private String publisher;

    private String genre;

    private LocalDate publishedDate;

    private String filePath; // путь к pdf-файлу

    // Геттеры и сеттеры

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }

    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getGenre() { return genre; }

    public void setGenre(String genre) { this.genre = genre; }

    public LocalDate getPublishedDate() { return publishedDate; }

    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }

    public String getFilePath() { return filePath; }

    public void setFilePath(String filePath) { this.filePath = filePath; }
}