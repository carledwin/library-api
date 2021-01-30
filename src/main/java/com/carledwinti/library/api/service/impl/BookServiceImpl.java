package com.carledwinti.library.api.service.impl;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.exception.BusinessException;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.repository.BookRepository;
import com.carledwinti.library.api.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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
    public Page<Book> findByFilter(Book bookFilter, Pageable pageRequest) {
        Example<Book> exampleBook = Example.of(bookFilter,
                ExampleMatcher.matching()
                        .withIgnoreCase()//irá ignorar Case nos valores passados par acada propriedade(author, title, isbn)
                        .withIgnoreNullValues()//irá ignorar as propriedades(author, title, isbn) cujo valor seja null
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //Neste caso irá buscar em cada
                        // propriedade do tipo String que foi passada(author, title, isbn) valores que contenham
                        // o valor passado em cada uma

        );
        return bookRepository.findAll(exampleBook, pageRequest);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return Optional.empty();
    }
}
