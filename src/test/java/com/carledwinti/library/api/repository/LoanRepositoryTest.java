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

        String isbn = "6678";
        Book book = Book.builder().author("Urntin").title("Sistema Solar").isbn(isbn).build();
        Book savedBook = testEntityManager.persist(book);

        //Loan loan = Loan.builder().book(savedBook).returned(true).loanDate(LocalDate.now()).isbn(isbn).customer("Amaro").build();
        //returned true or false
        Loan loan = Loan.builder().book(savedBook).returned(true).loanDate(LocalDate.now()).isbn(isbn).customer("Amaro").build();

        Loan savedLoan = testEntityManager.persist(loan);

        Boolean loanedBook = loanRepository.existsByBookAndNotReturned(savedBook);

        Assertions.assertThat(loanedBook).isTrue();

    }
}
