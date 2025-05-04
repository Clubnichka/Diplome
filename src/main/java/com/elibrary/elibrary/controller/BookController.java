package com.elibrary.elibrary.controller;

import com.elibrary.elibrary.model.Book;
import com.elibrary.elibrary.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
@CrossOrigin(origins = "http://localhost:3000")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> addBook(@RequestParam("file") MultipartFile file,
                                        @RequestParam("title") String title,
                                        @RequestParam("author") String author,
                                        @RequestParam("publisher") String publisher,
                                        @RequestParam("releaseDate") String releaseDate,
                                        @RequestParam("genre") String genre) throws IOException {

        // Проверка на тип файла (PDF)
        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body(null); // Возвращаем ошибку, если файл не PDF
        }

        // Создаем директорию для загрузки файлов, если она не существует
        String uploadDir = Paths.get("uploads").toAbsolutePath().normalize().toString();
        Files.createDirectories(Paths.get(uploadDir));

        // Сохраняем файл
        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + File.separator + fileName;
        File targetFile = new File(filePath);
        file.transferTo(targetFile); // Перемещаем файл в папку

        // Создаем новый объект книги
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setGenre(genre);
        book.setPublishedDate(LocalDate.parse(releaseDate));
        book.setFilePath(filePath);  // Сохраняем путь к файлу

        // Сохраняем книгу в БД
        Book savedBook = bookService.addBook(book);

        // Возвращаем успешно сохраненную книгу
        return ResponseEntity.ok(savedBook);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // Метод для чтения PDF-файла книги по ID
    @GetMapping("/{id}/read")
    public ResponseEntity<byte[]> readBook(@PathVariable Long id) {
        Optional<Book> bookOpt = bookService.getBookById(id);

        if (!bookOpt.isPresent()) {
            return ResponseEntity.notFound().build();  // Если книга не найдена
        }

        Book book = bookOpt.get();
        File bookFile = new File(book.getFilePath());

        // Проверяем, существует ли файл
        if (!bookFile.exists()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // Ошибка, если файл не найден
        }

        try {
            // Читаем файл в массив байтов
            byte[] fileContent = Files.readAllBytes(Paths.get(bookFile.getPath()));

            // Возвращаем файл в ответе с заголовками для отображения PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", bookFile.getName());  // Показываем PDF в браузере

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // Ошибка при чтении файла
        }
    }
}