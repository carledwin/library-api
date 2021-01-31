package com.carledwinti.library.api.repository;

import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.model.Loan;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    LoanRepository loanRepository;

    @Test
    @DisplayName("Deve verificar se existe emprestimo não devolvido para o livro")
    public void existsByBookAndNotReturned(){
        Book savedBook = createAndPersistLoan().getBook();
        Boolean loanedBook = loanRepository.existsByBookAndNotReturned(savedBook);
        Assertions.assertThat(loanedBook).isTrue();
    }


    @Test
    @DisplayName("Deve buscar loan pelo isbn do livro ou customer")
    public void findByBookIsbnOrCustomer(){
        Loan savadLoan = createAndPersistLoan();
        int page=0, size=12;
        Page<Loan> pageFoundLoan = loanRepository.findByBookIsbnOrCustomer("6678", "Amaro", PageRequest.of(page, size));
        Assertions.assertThat(pageFoundLoan.isEmpty()).isFalse();
        Assertions.assertThat(pageFoundLoan.getContent()).hasSize(1);
        Assertions.assertThat(pageFoundLoan.getPageable().getPageSize()).isEqualTo(12);
        Assertions.assertThat(pageFoundLoan.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(pageFoundLoan.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(pageFoundLoan.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(pageFoundLoan.getContent()).contains(savadLoan);
    }

    private Loan createAndPersistLoan() {
        String isbn = "6678";
        Book book = Book.builder().author("Urntin").title("Sistema Solar").isbn(isbn).build();
        Book savedBook = testEntityManager.persist(book);

        //Loan loan = Loan.builder().book(savedBook).returned(true).loanDate(LocalDate.now()).isbn(isbn).customer("Amaro").build();
        //returned true or false
        Loan loan = Loan.builder().book(savedBook).returned(true).loanDate(LocalDate.now()).isbn(isbn).customer("Amaro").build();

        Loan savedLoan = testEntityManager.persist(loan);
        return savedLoan;
    }
}
