package com.elibrary.elibrary.controller;

import com.elibrary.elibrary.model.Book;
import com.elibrary.elibrary.model.Tag;
import com.elibrary.elibrary.service.BookService;
import com.elibrary.elibrary.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
@CrossOrigin(origins = "http://localhost:3000")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private TagService tagService;  // Добавление сервиса для работы с тегами

    @GetMapping
    public List<Book> getAllBooks() {
        List<Book> books = bookService.getAllBooks();

        // Загружаем теги для каждой книги
        books.forEach(book -> book.setTags(tagService.getTagsForBook(book.getId())));

        return books;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(b -> {
            // Загружаем теги для книги по её ID
            b.setTags(tagService.getTagsForBook(b.getId()));
            return ResponseEntity.ok(b);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> addBook(@RequestParam("file") MultipartFile file,
                                        @RequestParam("title") String title,
                                        @RequestParam("author") String author,
                                        @RequestParam("publisher") String publisher,
                                        @RequestParam("releaseDate") String releaseDate,
                                        @RequestParam("genre") String genre,
                                        @RequestParam(value = "tags", required = false) List<String> tags) throws IOException {

        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body(null);
        }

        String uploadDir = Paths.get("uploads").toAbsolutePath().normalize().toString();
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + File.separator + fileName;
        file.transferTo(new File(filePath));

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setGenre(genre);
        book.setPublishedDate(LocalDate.parse(releaseDate));
        book.setFilePath(filePath);

        // Сохраняем книгу и связанные с ней теги
        Book savedBook = bookService.addBook(book, tags != null ? tags : new ArrayList<>());

        return ResponseEntity.ok(savedBook);
    }
    @GetMapping("/filter")
    public ResponseEntity<Page<Book>> filterBooks(
            @RequestParam(required = false) String genres,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Book> books = bookService.filterBooks(genres, author, yearFrom, yearTo, tags, page, size);
        return ResponseEntity.ok(books);
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