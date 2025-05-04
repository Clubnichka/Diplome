package com.elibrary.elibrary.service;

import com.elibrary.elibrary.model.Book;
import com.elibrary.elibrary.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    private static final String UPLOAD_DIR = "uploads/";

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    // Метод для загрузки файла PDF и сохранения его в базу данных
    public void uploadBookFile(Long bookId, MultipartFile file) throws IOException {
        // Поиск книги по ID
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (bookOptional.isEmpty()) {
            throw new RuntimeException("Book not found");
        }

        // Генерация уникального имени файла и сохранение на сервер
        Book book = bookOptional.get();
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(path.getParent());  // Создаём директорию, если она не существует
        Files.write(path, file.getBytes());  // Записываем файл на диск

        // Сохраняем путь к файлу в базе данных
        book.setFilePath(path.toString());
        bookRepository.save(book);
    }
}