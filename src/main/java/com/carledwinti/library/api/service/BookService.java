package com.carledwinti.library.api.service;

import com.carledwinti.library.api.model.Book;

import java.util.Optional;

public interface BookService {
    Book save(Book book);
    Optional<Book> getByid(Long id);
    void delete(Book book);
    Book update(Book book);
}
