package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.dto.BookDTO;
import com.carledwinti.library.api.dto.LoanDTO;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.model.Loan;
import com.carledwinti.library.api.service.BookService;
import com.carledwinti.library.api.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor //para inicializar(@Autowired) as propriedades da class sem precisar de um construtor
@Api("Book API")
@Slf4j
public class BookController {

    private final BookService bookService;
    private final ModelMapper modelMapper;
    private final LoanService loanService;

   /*
   private BookService bookService;
    private ModelMapper modelMapper; //existe a necessida de adicioná-lo ao context declarando
    // um @Bean na class de inicialização **LibraryApiApplication para que ela possa ser injetada aqui via constructor

    //criado o constructor para injetar o bookService no controller ao inves de utilizar o @Autowired
    public BookController(BookService bookService, ModelMapper modelMapper){
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }*/

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Creates a book")
    public BookDTO create(@Valid @RequestBody BookDTO bookDTO){
    log.info("creating a book for isbn: {}", bookDTO.getIsbn());
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
    @ApiOperation("Obtains a book by id")
    public BookDTO getBook(@PathVariable Long id){
        log.info("Obtaining details for book id: {}", id);
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
    @ApiOperation("Deletes a book by id")
    @ApiResponses({@ApiResponse(code=204, message = "Book successfully deleted")})
    public void deleteBook(@PathVariable Long id){
        log.info("Deleting a book by id: {}", id);
        //ele tenta obter um book com um .get() implicito
        // e caso não encontre retornamos uma exception com resonseStatus NOT_FOUND no orElseThrow
        Book book = bookService.getByid(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Updates a book by id")
    public BookDTO updateBook(@PathVariable Long id, @RequestBody @Valid BookDTO bookDTO){
        log.info("Updating a book by id: {}", id);
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

    //Neste caso o Spring vai tentar encaixar as propriedades author e title no objeto bookDTO os parametros passados
    // via queryParam e também as propriedades page e size ele vai tentar encaicar no objeto pageRequest(Pageable)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Obtains many Books by filters")
    public Page<BookDTO> getByFilter(BookDTO bookDTO, Pageable pageRequest){
        log.info("Obtaining a book by filter: {}", bookDTO.toString());
        Book bookFilter = modelMapper.map(bookDTO, Book.class);
        Page<Book> pageBook = bookService.findByFilter(bookFilter, pageRequest);
        List<BookDTO> bookDTOList =  pageBook.getContent().stream()
                                            .map(book -> modelMapper.map(book, BookDTO.class))
                                            .collect(Collectors.toList());
        return new PageImpl<BookDTO>(bookDTOList, pageRequest, pageBook.getTotalElements());
    }

    //MAPEAMENTO DE SUBRECURSO
    @GetMapping("/{id}/loans")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Obtains all Loans from Book by id book")
    public Page<LoanDTO> getAllLoanFromBook(@PathVariable Long id, Pageable pageable){
        log.info("Obtaining loans to book by id: {}", id);
        Book book = bookService.getByid(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> pageLoan = loanService.getLoansByBook(book, pageable);
        List<LoanDTO> loanDTOS = pageLoan.getContent().stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBookDTO(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(loanDTOS, pageable, pageLoan.getTotalElements());
    }

    @GetMapping("/teste-log")
    public String testeLog(){
        log.info("teste de log********************SUCCESS");
        log.warn("teste de log********************WARNING");
        log.error("teste de log********************ERROR");
        return "Teste de log....";
    }

}
