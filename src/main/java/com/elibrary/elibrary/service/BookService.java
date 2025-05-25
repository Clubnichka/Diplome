package com.elibrary.elibrary.service;

import com.elibrary.elibrary.mapper.BookMapper;
import com.elibrary.elibrary.model.Book;
import com.elibrary.elibrary.dto.BookDTO;
import com.elibrary.elibrary.model.BookView;
import com.elibrary.elibrary.model.Tag;
import com.elibrary.elibrary.model.User;
import com.elibrary.elibrary.repository.BookRepository;
import com.elibrary.elibrary.repository.BookViewRepository;
import com.elibrary.elibrary.repository.TagRepository;
import com.elibrary.elibrary.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookService {

    @Autowired private BookRepository bookRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private BookViewRepository bookViewRepository;
    @Autowired private UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/";

    public List<Book> getAllBooks() {
        return bookRepository.findAll().stream().toList();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    @Transactional
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
        if (bookOptional.isEmpty()) throw new RuntimeException("Book not found");

        Book book = bookOptional.get();
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        book.setFilePath(path.toString());

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            book.setCoverImage(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bookRepository.save(book);
    }

    public boolean hasAvailableCopies(Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> book.getAvailableCopies() > 0)
                .orElse(false);
    }

    public void decrementAvailableCopies(Long bookId) {
        bookRepository.findById(bookId).ifPresent(book -> {
            if (book.getAvailableCopies() > 0) {
                book.setAvailableCopies(book.getAvailableCopies() - 1);
                bookRepository.save(book);
            }
        });
    }

    public void incrementAvailableCopies(Long bookId) {
        bookRepository.findById(bookId).ifPresent(book -> {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        });
    }

    public Page<BookDTO> filterBooks(String genres, String author, Integer yearFrom, Integer yearTo, String tags, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findAll((root, query, cb) -> {
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
                Join<Book, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(tagJoin.get("name").in(tagList));
                query.groupBy(root.get("id"));
                query.having(cb.equal(cb.countDistinct(tagJoin.get("name")), tagList.size()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return books.map(BookMapper::toDTO);
    }

    public Page<BookDTO> searchBooks(String queryText, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Book> books = bookRepository.findAll((root, query, cb) -> {
            String lowerQuery = queryText.toLowerCase();

            Predicate titleExact = cb.equal(cb.lower(root.get("title")), lowerQuery);
            Predicate authorExact = cb.equal(cb.lower(root.get("author")), lowerQuery);
            Predicate genreExact = cb.equal(cb.lower(root.get("genre")), lowerQuery);

            Predicate exactMatch = cb.or(titleExact, authorExact, genreExact);

            Predicate titleLike = cb.like(cb.lower(root.get("title")), "%" + lowerQuery + "%");
            Predicate authorLike = cb.like(cb.lower(root.get("author")), "%" + lowerQuery + "%");
            Predicate genreLike = cb.like(cb.lower(root.get("genre")), "%" + lowerQuery + "%");

            Predicate partialMatch = cb.or(titleLike, authorLike, genreLike);

            return cb.or(exactMatch, partialMatch);
        }, pageable);

        return books.map(BookMapper::toDTO);
    }

    public void recordBookView(Long bookId, String username) {
        Book book = bookRepository.findById(bookId).orElse(null);
        User user = userRepository.findByUsername(username).orElse(null);
        if (book != null && user != null) {
            BookView view = new BookView(user, book, LocalDateTime.now());
            bookViewRepository.save(view);
        }
    }

    public Map<String, List<BookDTO>> getRecommendations(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return Collections.emptyMap();
        User user = userOpt.get();

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<BookView> views = bookViewRepository.findByUserAndViewedAtAfter(user, weekAgo);

        Map<String, Long> genreFreq = new HashMap<>();
        Map<String, Long> authorFreq = new HashMap<>();
        Map<String, Long> tagFreq = new HashMap<>();

        for (BookView view : views) {
            Book book = view.getBook();

            if (book.getGenre() != null) {
                genreFreq.put(book.getGenre(), genreFreq.getOrDefault(book.getGenre(), 0L) + 1);
            }

            if (book.getAuthor() != null) {
                authorFreq.put(book.getAuthor(), authorFreq.getOrDefault(book.getAuthor(), 0L) + 1);
            }

            if (book.getTags() != null) {
                for (Tag tag : book.getTags()) {
                    tagFreq.put(tag.getName(), tagFreq.getOrDefault(tag.getName(), 0L) + 1);
                }
            }
        }

        List<String> topGenres = genreFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3).map(Map.Entry::getKey).toList();

        List<String> topAuthors = authorFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3).map(Map.Entry::getKey).toList();

        List<String> topTags = tagFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3).map(Map.Entry::getKey).toList();

        Map<String, List<BookDTO>> result = new LinkedHashMap<>();
        if (!topGenres.isEmpty()) {
            result.put("По жанрам", bookRepository.findByGenreIn(topGenres)
                    .stream().map(BookMapper::toDTO).toList());
        }
        if (!topAuthors.isEmpty()) {
            result.put("По авторам", bookRepository.findByAuthorIn(topAuthors)
                    .stream().map(BookMapper::toDTO).toList());
        }
        if (!topTags.isEmpty()) {
            result.put("По тегам", bookRepository.findByTags_NameIn(topTags)
                    .stream().map(BookMapper::toDTO).toList());
        }

        return result;
    }

    @Transactional
    public Map<String, Object> getDetailedRecommendations(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return Collections.emptyMap();
        User user = userOpt.get();

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<BookView> views = bookViewRepository.findByUserAndViewedAtAfter(user, weekAgo);

        Map<String, Long> genreFreq = new HashMap<>();
        Map<String, Long> authorFreq = new HashMap<>();
        Map<String, Long> tagFreq = new HashMap<>();

        for (BookView view : views) {
            Book book = view.getBook();

            if (book.getGenre() != null) {
                genreFreq.put(book.getGenre(), genreFreq.getOrDefault(book.getGenre(), 0L) + 1);
            }

            if (book.getAuthor() != null) {
                authorFreq.put(book.getAuthor(), authorFreq.getOrDefault(book.getAuthor(), 0L) + 1);
            }

            if (book.getTags() != null) {
                for (var tag : book.getTags()) {
                    tagFreq.put(tag.getName(), tagFreq.getOrDefault(tag.getName(), 0L) + 1);
                }
            }
        }

        List<String> topGenres = genreFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3).map(Map.Entry::getKey).toList();

        List<String> topAuthors = authorFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3).map(Map.Entry::getKey).toList();

        List<String> topTags = tagFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3).map(Map.Entry::getKey).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        if (!topGenres.isEmpty()) {
            result.put("genres", topGenres);
            result.put("booksByGenres", bookRepository.findByGenreIn(topGenres)
                    .stream().map(BookMapper::toDTO).toList());
        }
        if (!topAuthors.isEmpty()) {
            result.put("authors", topAuthors);
            result.put("booksByAuthors", bookRepository.findByAuthorIn(topAuthors)
                    .stream().map(BookMapper::toDTO).toList());
        }
        if (!topTags.isEmpty()) {
            result.put("tags", topTags);
            result.put("booksByTags", bookRepository.findByTags_NameIn(topTags)
                    .stream().map(BookMapper::toDTO).toList());
        }

        return result;
    }

    @Transactional
    public void generateCoversForExistingBooks() {
        List<Book> books = bookRepository.findAll();
        for (Book book : books) {
            if (book.getCoverImage() != null && book.getCoverImage().length > 0) {
                continue;
            }
            try {
                byte[] cover = extractCoverFromPdf(book.getFilePath());
                if (cover != null) {
                    book.setCoverImage(cover);
                    bookRepository.save(book);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при обработке книги: " + book.getTitle());
                e.printStackTrace();
            }
        }
    }

    private byte[] extractCoverFromPdf(String filePath) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}