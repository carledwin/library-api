package com.carledwinti.library.api.service;

import com.carledwinti.library.api.dto.LoanFilterDTO;
import com.carledwinti.library.api.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoanService {
    Optional<Loan> save(Loan any);
    Optional<Loan> getById(Long id);
    Optional<Loan> update(Loan loan);
    Page<Loan> findByFilter(Loan loanFilter, Pageable pageable);
}
