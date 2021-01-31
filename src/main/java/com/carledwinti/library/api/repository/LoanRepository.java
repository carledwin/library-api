package com.carledwinti.library.api.repository;

import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query(value = "select " +
                    " case when( count(loan.id) > 0 ) then true else false end " +
                    " from Loan loan " +
                    " where loan.book = :book " +
                    " and ( loan.returned is null or loan.returned is not false ) "
            , nativeQuery = false)
    Boolean existsByBookAndNotReturned(@Param("book") Book book);
    Page<Loan> findByBookIsbnOrCustomer(String isbn, String customer, Pageable pageable);
}
