package com.carledwinti.library.api.service.impl;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.dto.LoanFilterDTO;
import com.carledwinti.library.api.exception.BusinessException;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.model.Loan;
import com.carledwinti.library.api.repository.LoanRepository;
import com.carledwinti.library.api.service.LoanService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        if(loanRepository.existsByBookAndNotReturned(loan.getBook())){
            throw new BusinessException(ConstantsError.MSG_ERROR_BOOK_ALREADY_LOANED);
        }
        return Optional.of(this.loanRepository.save(loan));
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public Optional<Loan> update(Loan loan) {
        Loan existentLoan = loanRepository.findById(loan.getId())
                .orElseThrow(() -> new BusinessException(ConstantsError.MSG_ERROR_RETURN_LOAN_ID_NOTFOUND));

        existentLoan.setReturned(loan.getReturned());
        Loan updatedLoan = loanRepository.save(existentLoan);
        return Optional.of(updatedLoan);
    }

    @Override
    public Page<Loan> findByFilter(Loan loanFilter, Pageable pageable) {
        return loanRepository.findByBookIsbnOrCustomer(loanFilter.getIsbn(), loanFilter.getCustomer(), pageable);
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
        return loanRepository.findByBook(book, pageable);
    }


}
