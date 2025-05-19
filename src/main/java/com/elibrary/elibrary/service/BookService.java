package com.elibrary.elibrary.service;

import com.elibrary.elibrary.model.Book;
import com.elibrary.elibrary.model.Tag;
import com.elibrary.elibrary.repository.BookRepository;
import com.elibrary.elibrary.repository.TagRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TagRepository tagRepository;

    private static final String UPLOAD_DIR = "uploads/";

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book addBook(Book book, List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(tagName)));
            tags.add(tag);
        }
        book.setTags(tags);
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public void uploadBookFile(Long bookId, MultipartFile file) throws IOException {
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (bookOptional.isEmpty()) {
            throw new RuntimeException("Book not found");
        }

        Book book = bookOptional.get();
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        book.setFilePath(path.toString());
        bookRepository.save(book);
    }

    //  Проверка доступных экземпляров
    public boolean hasAvailableCopies(Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> book.getAvailableCopies() > 0)
                .orElse(false);
    }

    // Уменьшение количества экземпляров
    public void decrementAvailableCopies(Long bookId) {
        bookRepository.findById(bookId).ifPresent(book -> {
            if (book.getAvailableCopies() > 0) {
                book.setAvailableCopies(book.getAvailableCopies() - 1);
                bookRepository.save(book);
            }
        });
    }

    // Увеличение количества экземпляров (например, при возврате книги)
    public void incrementAvailableCopies(Long bookId) {
        bookRepository.findById(bookId).ifPresent(book -> {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        });
    }
    public Page<Book> filterBooks(String genres, String author, Integer yearFrom, Integer yearTo, String tags, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (genres != null && !genres.isEmpty()) {
                List<String> genreList = Arrays.asList(genres.split(","));
                predicates.add(root.get("genre").in(genreList));
            }

            if (author != null && !author.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
            }

            if (yearFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publishedDate"), LocalDate.of(yearFrom, 1, 1)));
            }

            if (yearTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("publishedDate"), LocalDate.of(yearTo, 12, 31)));
            }

            if (tags != null && !tags.isEmpty()) {
                List<String> tagList = Arrays.asList(tags.split(","));
                Join<Book, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(tagJoin.get("name").in(tagList));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }
}