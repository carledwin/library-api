package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.dto.LoanDTO;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.model.Loan;
import com.carledwinti.library.api.service.BookService;
import com.carledwinti.library.api.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor //cria um construtor com as propriedades da class *** para que funcione as propriedades precisam
//estar marcadas como final
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final ModelMapper modelMapper;
    /*
    private LoanService loanService;
    private BookService bookService;
    private ModelMapper modelMapper;

    Substituido por @RequiredArgsConstructor
    public LoanController(LoanService loanService, BookService bookService, ModelMapper modelMapper){
        this.loanService = loanService;
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }*/

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDTO create(@RequestBody LoanDTO loanDTO){
        Book book = bookService.getBookByIsbn(loanDTO.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST
                        , ConstantsError.MSG_ERROR_BOOK_NOT_FOUND_FOR_ISBN));

        Loan loan = modelMapper.map(loanDTO, Loan.class);
        loan.setLoanDate(LocalDate.now());
        loan.setBook(book);

        Optional<Loan> optionalLoan = loanService.save(loan);
        return modelMapper.map(optionalLoan.get(), LoanDTO.class);
    }
}
