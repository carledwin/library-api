package com.carledwinti.library.api.service;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.dto.LoanFilterDTO;
import com.carledwinti.library.api.exception.BusinessException;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.model.Loan;
import com.carledwinti.library.api.repository.LoanRepository;
import com.carledwinti.library.api.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    @MockBean
    LoanRepository loanRepository;

    LoanService loanService;

    @BeforeEach
    public void setUp(){
        this.loanService = new LoanServiceImpl(loanRepository);
    }

    @Test
    @DisplayName("Deve salvar um emprestimo")
    public void saveLoan(){
        //scenario
        Loan loan = savedLoan();
        loan.setId(null);
        Loan savedLoanMock = savedLoan();

        //mock
        Mockito.when(loanRepository.save(loan)).thenReturn(savedLoanMock);
        Mockito.when(loanRepository.existsByBookAndNotReturned(loan.getBook())).thenReturn(false);
        //execution
        //caso este método não tenha sido implementado com a chamada para loanRepository.save(..) que foi mockado
        //será retornado o erro org.opentest4j.AssertionFailedError:
        //Expected :true Actual :false
        Optional<Loan> savedLoan = loanService.save(loan);

        //verification
        Assertions.assertThat(savedLoan.isPresent()).isTrue();
        Assertions.assertThat(savedLoan.get().getBook()).isEqualTo(savedLoanMock.getBook());
        Assertions.assertThat(savedLoan.get().getId()).isEqualTo(savedLoanMock.getId());
        Assertions.assertThat(savedLoan.get().getCustomer()).isEqualTo(savedLoanMock.getCustomer());
        Assertions.assertThat(savedLoan.get().getIsbn()).isEqualTo(savedLoanMock.getIsbn());
        Assertions.assertThat(savedLoan.get().getLoanDate()).isEqualTo(LocalDate.now());

        //precisamos garantir que o save nunca erá executado quando este erro for lançado
        Mockito.verify(loanRepository, Mockito.times(1)).existsByBookAndNotReturned(loan.getBook());
        Mockito.verify(loanRepository, Mockito.times(1)).save(loan);
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um emprestimo(loan) já emprestado")
    public void loanLoaned(){
        //scenario
        Loan loan = savedLoan();
        loan.setId(null);

        //mock
        Mockito.when(loanRepository.existsByBookAndNotReturned(loan.getBook())).thenReturn(true);

        //execution
        //caso este método não tenha sido implementado com a chamada para loanRepository.save(..) que foi mockado
        //será retornado o erro org.opentest4j.AssertionFailedError:
        //Expected :true Actual :false

        //caso a validação*** não tenha sido implementada no serviceImpl dentro do metodo save(..) não será lançada a exception, ocasionando o erro
        //java.lang.AssertionError:
        //Expecting:
        //  <java.lang.NullPointerException>
        //to be an instance of:
        //  <com.carledwinti.library.api.exception.BusinessException>
        //but was:
        //  <"java.lang.NullPointerException
        //	at java.util.Objects.requireNonNull(Objects.java:203)
        //
        // ***validação*** if(loanRepository.existsByBookAndNotReturned(loan)){
        //            throw new BusinessException(ConstantsError.MSG_ERROR_BOOK_ALREADY_LOANED);
        //        }

        Throwable throwable = Assertions.catchThrowable(() -> loanService.save(loan));

        Assertions.assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ConstantsError.MSG_ERROR_BOOK_ALREADY_LOANED);

        //precisamos garantir que o save nunca erá executado quando este erro for lançado
        Mockito.verify(loanRepository, Mockito.times(1)).existsByBookAndNotReturned(loan.getBook());
        Mockito.verify(loanRepository, Mockito.never()).save(loan);
    }

    @Test
    @DisplayName("Deve obter as informações de loan pelo Id")
    public void getLoanByid(){
        //scenario
        Long loanId = 1l;
        Loan existentLoan = savedLoan();

        //mock
        Mockito.when(loanRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(existentLoan));

        //execution
        Optional<Loan> foundLoan = loanService.getById(loanId);

        //verification
        Assertions.assertThat(foundLoan.get()).isEqualTo(existentLoan);
        Assertions.assertThat(foundLoan.isPresent()).isTrue();
        Mockito.verify(loanRepository, Mockito.times(1)).findById(loanId);
    }

    @Test
    @DisplayName("Deve atualizar um loan")
    public void updateLoan(){
        //scenario
        Loan existentLoan = savedLoan();
        Loan updatedLoan = savedLoan();
        updatedLoan.setReturned(true);

        //mock
        Mockito.when(loanRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(existentLoan));
        Mockito.when(loanRepository.save(existentLoan)).thenReturn(updatedLoan);

        //execution
        Optional<Loan> loan = loanService.update(existentLoan);

        //verification
        Assertions.assertThat(loan.isPresent()).isTrue();
        Assertions.assertThat(loan.get()).isEqualTo(updatedLoan);
        Assertions.assertThat(loan.get().getReturned()).isTrue();
        Mockito.verify(loanRepository, Mockito.times(1)).findById(existentLoan.getId());
        Mockito.verify(loanRepository, Mockito.times(1)).save(existentLoan);
    }

    private Loan savedLoan(){
        Book book = existentBook();
        return Loan.builder().id(1l)
                .isbn("123")
                .customer("Lariano")
                .book(book)
                .returned(false)
                .loanDate(LocalDate.now())
                .build();
    }

    private Book existentBook(){
        return Book.builder().id(5678l).title("Antenor Santanará").isbn("123").author("Milanes").build();
    }
}
