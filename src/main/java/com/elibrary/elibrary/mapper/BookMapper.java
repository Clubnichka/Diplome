package com.elibrary.elibrary.mapper;

import com.elibrary.elibrary.dto.BookDTO;
import com.elibrary.elibrary.model.Book;

import java.util.Base64;
import java.util.stream.Collectors;

public class BookMapper {

    public static BookDTO toDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPublishedDate(book.getPublishedDate());
        dto.setGenre(book.getGenre());

        if (book.getTags() != null) {
            dto.setTags(book.getTags().stream().map(tag -> tag.getName()).collect(Collectors.toList()));
        }

        if (book.getCoverImage() != null) {
            dto.setCoverBase64(Base64.getEncoder().encodeToString(book.getCoverImage()));
        }

        return dto;
    }
}