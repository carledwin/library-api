package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.dto.BookDTO;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BoockController {

    private BookService bookService;

    //criado o constructor para injetar o bookService no controller ao inves de utilizar o @Autowired
    public BoockController(BookService bookService){
        this.bookService = bookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody BookDTO bookDTO){
        Book bookEntity = Book.builder()
                .author(bookDTO.getAuthor())
                .title(bookDTO.getTitle())
                .isbn(bookDTO.getIsbn())
                .build();

        bookEntity = bookService.save(bookEntity);

        return BookDTO.builder()
                .id(bookEntity.getId())
                .author(bookEntity.getAuthor())
                .title(bookEntity.getTitle())
                .isbn(bookEntity.getIsbn())
                .build();
    }
}
