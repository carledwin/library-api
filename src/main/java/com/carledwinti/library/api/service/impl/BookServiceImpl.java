package com.carledwinti.library.api.service.impl;

import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.repository.BookRepository;
import com.carledwinti.library.api.service.BookService;
import org.springframework.stereotype.Service;

@Service//Para que essa class seja gerenciada pelo Spring framework
public class BookServiceImpl implements BookService {

    private BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository){
        this.bookRepository = bookRepository;
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }
}
