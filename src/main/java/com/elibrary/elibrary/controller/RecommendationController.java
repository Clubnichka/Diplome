package com.elibrary.elibrary.controller;

import com.elibrary.elibrary.model.Book;
import com.elibrary.elibrary.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<Map<String, Map<String, Object>>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        bookService.generateCoversForExistingBooks();
        String username = userDetails.getUsername();
        Map<String, Object> recommendationsRaw = bookService.getDetailedRecommendations(username);

        // Приведение к нужной структуре
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        if (recommendationsRaw.containsKey("genres")) {
            result.put("По жанрам", Map.of(
                    "genres", recommendationsRaw.get("genres"),
                    "books", recommendationsRaw.get("booksByGenres")
            ));
        }

        if (recommendationsRaw.containsKey("authors")) {
            result.put("По авторам", Map.of(
                    "authors", recommendationsRaw.get("authors"),
                    "books", recommendationsRaw.get("booksByAuthors")
            ));
        }

        if (recommendationsRaw.containsKey("tags")) {
            result.put("По тегам", Map.of(
                    "tags", recommendationsRaw.get("tags"),
                    "books", recommendationsRaw.get("booksByTags")
            ));
        }

        return ResponseEntity.ok(result);
    }
}