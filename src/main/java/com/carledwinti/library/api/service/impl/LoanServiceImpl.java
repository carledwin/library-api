package com.carledwinti.library.api.service.impl;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.exception.BusinessException;
import com.carledwinti.library.api.model.Loan;
import com.carledwinti.library.api.repository.LoanRepository;
import com.carledwinti.library.api.service.LoanService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {

    private LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Optional<Loan> save(Loan loan) {
        if(loanRepository.existsByBookAndNotReturned(loan)){
            throw new BusinessException(ConstantsError.MSG_ERROR_BOOK_ALREADY_LOANED);
        }
        return Optional.of(this.loanRepository.save(loan));
    }
}
