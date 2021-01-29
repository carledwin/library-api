package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.dto.BookDTO;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BoockController extends BaseController{

    private BookService bookService;
    private ModelMapper modelMapper; //existe a necessida de adicioná-lo ao context declarando
    // um @Bean na class de inicialização **LibraryApiApplication para que ela possa ser injetada aqui via constructor

    //criado o constructor para injetar o bookService no controller ao inves de utilizar o @Autowired
    public BoockController(BookService bookService, ModelMapper modelMapper){
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@Valid @RequestBody BookDTO bookDTO){

    /*caso seja enviado um objeto com todas as propriedades vazias irá lançar
    org.springframework.web.util.NestedServletException: Request processing failed;
     nested exception is java.lang.IllegalArgumentException: source cannot be null, isso porque o
    modelMapper não aceitará um objeto com propriedades 'todas' vazias
    Será necessario anotar o @RequestBody com @Valid(valida a 'integridade' da requisição) e anotar alguns campos com validations do
    javax.validation ex.:javax.validation.constraints.NotEmpty;
    Desta forma o Spring não permitirá que a requisição aconteça*/

//        Book bookEntity = Book.builder()
//                .author(bookDTO.getAuthor())
//                .title(bookDTO.getTitle())
//                .isbn(bookDTO.getIsbn())
//                .build();

        //converter via modelMapper
        Book bookEntity = modelMapper.map(bookDTO, Book.class);

        bookEntity = bookService.save(bookEntity);

//        return BookDTO.builder()
//                .id(bookEntity.getId())
//                .author(bookEntity.getAuthor())
//                .title(bookEntity.getTitle())
//                .isbn(bookEntity.getIsbn())
//                .build();

        return modelMapper.map(bookEntity, BookDTO.class);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO getBook(@PathVariable Long id){
        //caso não retorne nada e não tenha tratamento irá retornar a exception --> Caused by: java.util.NoSuchElementException:
        // No value present
        //Optional<Book> bookOptional = bookService.getByid(id);

        //Vamos utilizar conforme a baixo para conseguirmos tratar o retorno de Option empty e retornar uma exception com codigo de status
        return bookService.getByid(id)
                .map(book -> modelMapper.map(book, BookDTO.class)) //mapea a entity book para bookDTO caso seja encontrado
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)); //retorna uma Exception Spring com
                                                // responseStatus customisado caso não encontre a entity na base de dados
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id){
        //ele tenta obter um book com um .get() implicito
        // e caso não encontre retornamos uma exception com resonseStatus NOT_FOUND no orElseThrow
        Book book = bookService.getByid(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO){
        return bookService.getByid(id).map(existentBook -> {
            //set
            existentBook.setAuthor(bookDTO.getAuthor());
            existentBook.setTitle(bookDTO.getTitle());
            //update
            existentBook = bookService.update(existentBook);
            //map to return DTO
            return modelMapper.map(existentBook, BookDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
