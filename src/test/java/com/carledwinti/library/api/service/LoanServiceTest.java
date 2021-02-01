package com.carledwinti.library.api.service;

import com.carledwinti.library.api.constants.ConstantsError;
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
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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

    @Test
    @DisplayName("Deve filtrar loan by filter")
    public void findByfilter(){
        //scenario
        int page=0, size=15, total=1;
        PageRequest pageRequest = PageRequest.of(page, size);
        Loan loanFilter = Loan.builder().isbn("").customer("").build();
        List<Loan> loanFindList = Arrays.asList(loanFilter);
        Page<Loan> pageFindLoan = new PageImpl<Loan>(loanFindList, pageRequest, total);
        Loan loan = savedLoan();
        List<Loan> loanFoundList = Arrays.asList(loan);
        Page<Loan> pageFoundLoan = new PageImpl<Loan>(loanFoundList, pageRequest, total);

        //mock
        Mockito.when(loanRepository.findByBookIsbnOrCustomer(Mockito.anyString(),
                                                             Mockito.anyString(),
                                                             Mockito.any(PageRequest.class)))
                                    .thenReturn(pageFoundLoan);

        //execution
        Page<Loan> foundPageLoan = loanService.findByFilter(loanFilter, pageFindLoan.getPageable());

        //verification
        Assertions.assertThat(foundPageLoan).isNotNull();
        Assertions.assertThat(foundPageLoan.isEmpty()).isFalse();
        Assertions.assertThat(foundPageLoan.getContent().isEmpty()).isFalse();
        Assertions.assertThat(foundPageLoan.getContent().get(0).getId()).isEqualTo(savedLoan().getId());
        Assertions.assertThat(foundPageLoan.getSize()).isEqualTo(size);
        Assertions.assertThat(foundPageLoan.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(foundPageLoan.getTotalElements()).isEqualTo(total);

        Mockito.verify(loanRepository, Mockito.times(1))
                .findByBookIsbnOrCustomer(loanFilter.getIsbn(), loanFilter.getCustomer(), pageRequest);
    }

    @Test
    @DisplayName("Deve buscar todos os emprestimos atrasados a mais de 3 dias e ainda não foram entregues")
    public void loanOverdueForMoreThan3Days(){
        //scenario
        Integer daysOfOverdue = 4;

        //mock
        Mockito.when(loanRepository.findByLoanDateLessThanAndNotReturned(Mockito.any(LocalDate.class)))
        .thenReturn(createExistentListOptionalLoanOverdue());
        //execution
        List<Optional<Loan>> foundOptionalLoanList = loanService.getAllOverdueLoans(daysOfOverdue);
        Assertions.assertThat(foundOptionalLoanList.isEmpty()).isFalse();
        Assertions.assertThat(foundOptionalLoanList.size()).isEqualTo(3);
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

    private List<Optional<Loan>> createExistentListOptionalLoanOverdue(){
        Book book = existentBook();
        Loan loan1 = Loan.builder().id(1l)
                .isbn("123")
                .customer("Lariano")
                .book(book)
                .returned(false)
                .loanDate(LocalDate.now().minusDays(10))
                .build();
        Loan loan2 = Loan.builder().id(41l)
                .isbn("123")
                .customer("Chcagos")
                .book(book)
                .returned(false)
                .loanDate(LocalDate.now().minusDays(8))
                .build();

        Book book2 = Book.builder().id(55l).title("Selva da Manada").isbn("12553").author("Antunbes").build();
        Loan loan3 = Loan.builder().id(13l)
                .isbn("12553")
                .customer("Frendess")
                .book(book2)
                .returned(false)
                .loanDate(LocalDate.now().minusDays(14))
                .build();

        return Arrays.asList(Optional.of(loan1), Optional.of(loan2), Optional.of(loan3));
    }
}
