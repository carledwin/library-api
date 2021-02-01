package com.carledwinti.library.api.service;

import java.util.List;

public interface EmailService {
    void sendEmailToLoansOverdue(String message, List<String> emailFromCustomersWithLoanOverdue);
}
