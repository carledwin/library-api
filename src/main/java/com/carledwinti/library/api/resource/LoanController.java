package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.dto.BookDTO;
import com.carledwinti.library.api.dto.LoanDTO;
import com.carledwinti.library.api.dto.LoanFilterDTO;
import com.carledwinti.library.api.dto.ReturnedLoanDTO;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.model.Loan;
import com.carledwinti.library.api.service.BookService;
import com.carledwinti.library.api.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public LoanDTO create(@Valid @RequestBody LoanDTO loanDTO){
        Book book = bookService.getBookByIsbn(loanDTO.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST
                        , ConstantsError.MSG_ERROR_BOOK_NOT_FOUND_FOR_ISBN));

        Loan loan = modelMapper.map(loanDTO, Loan.class);
        loan.setLoanDate(LocalDate.now());
        loan.setBook(book);

        Optional<Loan> optionalLoan = loanService.save(loan);
        return modelMapper.map(optionalLoan.get(), LoanDTO.class);
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public void getBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO returnedLoanDTO){
        Loan loan = loanService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ConstantsError.MSG_ERROR_RETURN_LOAN_ID_NOTFOUND));
        loan.setReturned(returnedLoanDTO.getReturned());
        loanService.update(loan);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<LoanDTO> findByFilter(LoanFilterDTO loanFilterDTO, Pageable pageable){
        Loan loanFilter = modelMapper.map(loanFilterDTO, Loan.class);
        Page<Loan> pageLoan = loanService.findByFilter(loanFilter, pageable);
        List<LoanDTO> loanList = pageLoan.getContent().stream()
                .map(loan -> {
                    //Exemplo de mapeamento/conversão de objetos com os mesmos campos **** e subObjetos junto **********
                    Book book = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBookDTO(bookDTO);
                    return loanDTO;
                })
                .collect(Collectors.toList());
        return new PageImpl<LoanDTO>(loanList, pageable, pageLoan.getTotalElements());
    }
}
