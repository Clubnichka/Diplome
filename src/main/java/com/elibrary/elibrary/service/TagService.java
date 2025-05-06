package com.elibrary.elibrary.service;

import com.elibrary.elibrary.model.Book;
import com.elibrary.elibrary.model.Tag;
import com.elibrary.elibrary.repository.BookRepository;
import com.elibrary.elibrary.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private BookRepository bookRepository;
    // Метод для получения тегов по списку имен
    public Set<Tag> getTagsForBook(List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();

        // Ищем каждый тег по имени, если он существует — добавляем, если нет — создаем новый
        for (String tagName : tagNames) {
            tagRepository.findByName(tagName).ifPresentOrElse(
                    tags::add,
                    () -> tags.add(tagRepository.save(new Tag(tagName)))
            );
        }

        return tags;
    }

    // Метод для получения тегов по ID книги
    public Set<Tag> getTagsForBook(Long bookId) {
        Optional<Book> bookOptional = bookRepository.findById(bookId);

        // Если книга существует, возвращаем её теги
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            return book.getTags(); // Возвращаем теги книги
        }

        // Если книга не найдена, возвращаем пустое множество
        return new HashSet<>();
    }
}
