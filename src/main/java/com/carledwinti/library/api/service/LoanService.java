package com.carledwinti.library.api.service;

import com.carledwinti.library.api.model.Loan;

import java.util.Optional;

public interface LoanService {
    Optional<Loan> save(Loan any);
}
