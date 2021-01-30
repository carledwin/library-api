package com.carledwinti.library.api.service.impl;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.exception.BusinessException;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.repository.BookRepository;
import com.carledwinti.library.api.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    @Override
    public Optional<Book> getByid(Long id) {
        if(id == null){
            throw new IllegalArgumentException(ConstantsError.MSG_ERROR_ID_CANT_BE_NULL);
        }
        return bookRepository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if(book == null || book.getId() == null){
            throw new IllegalArgumentException(ConstantsError.MSG_ERROR_BOOK_AND_ID_CANT_BE_NULL);
        }
        bookRepository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if(book == null || book.getId() == null){
            throw new IllegalArgumentException(ConstantsError.MSG_ERROR_BOOK_AND_ID_CANT_BE_NULL);
        }
        return bookRepository.save(book);
    }

    @Override
    public Page<Book> find(Book bookFilter, Pageable pageRequest) {
        return null;
    }
}
