package com.carledwinti.library.api.repository;

import com.carledwinti.library.api.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
