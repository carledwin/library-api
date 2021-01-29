package com.carledwinti.library.api.service.impl;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.exception.BusinessException;
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
        if(bookRepository.existsByIsbn(book.getIsbn())){
            throw new BusinessException(ConstantsError.MSG_ERROR_ISBN_ALREADY_EXISTS);
        }
        return bookRepository.save(book);
    }
}
