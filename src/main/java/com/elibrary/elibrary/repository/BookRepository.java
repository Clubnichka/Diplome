package com.elibrary.elibrary.repository;

import com.elibrary.elibrary.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    List<Book> findByGenreIn(List<String> genres);
    List<Book> findByAuthorIn(List<String> authors);
    List<Book> findByTags_NameIn(List<String> tagNames);
}