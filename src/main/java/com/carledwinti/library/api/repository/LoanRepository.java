package com.carledwinti.library.api.repository;

import com.carledwinti.library.api.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query(value = "select case when(count(loan.id) > 0) " +
                    "then true " +
                    "else false " +
                    "end " +
                    "from Loan loan", nativeQuery = false)
    Boolean existsByBookAndNotReturned(Loan loan);
}
