package com.carledwinti.library.api.service;

import com.carledwinti.library.api.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {
    Book save(Book book);
    Optional<Book> getByid(Long id);
    void delete(Book book);
    Book update(Book book);

    Page<Book> findByFilter(Book bookFilter, Pageable pageRequest);
}
