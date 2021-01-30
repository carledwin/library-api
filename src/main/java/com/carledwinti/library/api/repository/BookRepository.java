package com.carledwinti.library.api.repository;

import com.carledwinti.library.api.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    //Por se tratar de um QueryMethod, utilizamos palavras chaves e não existe a necessidade de implementar o método
    //ele será implemantado pelo Spring Data em tempo de runtime retornando true ou false
    boolean existsByIsbn(String isbn);
    Optional<Book> findByIsbn(String isbn);
}
