package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.dto.BookDTO;
import com.carledwinti.library.api.exception.ApiErrors;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BoockController {

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

    //aqui vamos utilizar o ExceptionHandler para tratar exceptions da nossa api
    //essa exception ApiErros automaticamente será lançada toda vez que o @Valid tentar validar uma request e ela não for valida,
    //pois ao criar um method anotado com @ExceptionHandler ele irá interceptar o @Valid e capturara o erro
    //mas, somente isso não basta, agora ele irá retornar a lista de errors, mas irá com ResponseStatus default do path, neste caso 200.
    // Como precisaremos de um retorno 400 BadReques também precisamos anotar o método handleValidationExceptions com @ReponseStatus
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException methodArgumentNotValidException){

        //será recebido como parametro o retorno da exception com um BindResult que contém todas as mensagens de erros
        BindingResult bindingResult = methodArgumentNotValidException.getBindingResult();

        //contém todos os erros que ocorreram na validação
        List<ObjectError> objectErrors = bindingResult.getAllErrors();

        return new ApiErrors(bindingResult);

    }
}
