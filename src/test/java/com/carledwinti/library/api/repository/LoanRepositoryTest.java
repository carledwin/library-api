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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    BookRepository bookRepository;

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

    @Test
    @DisplayName("Dever retornar todos os emprestimos vencidos a mais de n dias")
    public void findLoanOverdueMoreThanNDays(){

        List<Optional<Loan>> savedLoansOverdue = createListOptionalLoanOverdue();

        Assertions.assertThat(savedLoansOverdue.isEmpty()).isFalse();
        Assertions.assertThat(savedLoansOverdue.size()).isEqualTo(5);
        Assertions.assertThat(savedLoansOverdue.get(0).get().getIsbn()).isEqualTo("123");
        Assertions.assertThat(savedLoansOverdue.get(1).get().getIsbn()).isEqualTo("123");
        Assertions.assertThat(savedLoansOverdue.get(2).get().getIsbn()).isEqualTo("12553");
        Assertions.assertThat(savedLoansOverdue.get(3).get().getIsbn()).isEqualTo("12553");
        Assertions.assertThat(savedLoansOverdue.get(4).get().getIsbn()).isEqualTo("12553");

        LocalDate threeDaysOfOverdue = LocalDate.now().minusDays(3);
        List<Optional<Loan>> foundLoansOverdue = loanRepository.findByLoanDateLessThanAndNotReturned(threeDaysOfOverdue);

        Assertions.assertThat(foundLoansOverdue.isEmpty()).isFalse();
        Assertions.assertThat(foundLoansOverdue.size()).isEqualTo(3);
        Assertions.assertThat(foundLoansOverdue.get(0).get().getIsbn()).isEqualTo("123");
        Assertions.assertThat(foundLoansOverdue.get(1).get().getIsbn()).isEqualTo("123");
        Assertions.assertThat(foundLoansOverdue.get(2).get().getIsbn()).isEqualTo("12553");
    }

    private List<Optional<Loan>> createListOptionalLoanOverdue(){
        Book book = Book.builder().title("Mirados Arabes").isbn("123").author("Lembratess").build();
        book = bookRepository.save(book);

        Loan loan1 = Loan.builder().isbn("123")
                .customer("Lariano")
                .book(book)
                .returned(false)
                .loanDate(LocalDate.now().minusDays(10))
                .build();
        loan1 = loanRepository.save(loan1);

        Loan loan2 = Loan.builder().isbn("123")
                .customer("Chcagos")
                .book(book)
                .returned(false)
                .loanDate(LocalDate.now().minusDays(8))
                .build();
        loan2 = loanRepository.save(loan2);

        Book book2 = Book.builder().title("Selva da Manada").isbn("12553").author("Antunbes").build();
        book2 = bookRepository.save(book2);


        Loan loan3 = Loan.builder().isbn("12553")
                .customer("Frendess")
                .book(book2)
                .returned(false)
                .loanDate(LocalDate.now().minusDays(14))
                .build();
        loan3 = loanRepository.save(loan3);

        Loan loan4 = Loan.builder().isbn("12553")
                .customer("kanus")
                .book(book2)
                .returned(false)
                .loanDate(LocalDate.now())
                .build();
        loan4 = loanRepository.save(loan4);

        Loan loan5 = Loan.builder().isbn("12553")
                .customer("Lima")
                .book(book2)
                .returned(true)
                .loanDate(LocalDate.now().minusDays(50))
                .build();
        loan5 = loanRepository.save(loan5);

        int total = loanRepository.findAll().size();
        System.out.println("Total: " + total);
        Assertions.assertThat(total).isEqualTo(5);

        return Arrays.asList(Optional.of(loan1), Optional.of(loan2), Optional.of(loan3), Optional.of(loan4), Optional.of(loan5));
    }
}
