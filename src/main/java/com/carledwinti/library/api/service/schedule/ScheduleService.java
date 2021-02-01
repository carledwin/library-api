package com.carledwinti.library.api.service.schedule;

import com.carledwinti.library.api.model.Loan;
import com.carledwinti.library.api.service.EmailService;
import com.carledwinti.library.api.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor //par injetar todas as propriedades final
// sem precisar de @Autowired ou constructor para injeção
public class ScheduleService {

    //será executado ao 0segundo 0 minuto 0 hora 1/1 todos os dias * qualquer mês ? qualquer ano
    private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";
    private static final String CRON_LATE_LOANS_SEGUNDOS = "1/30 * * * * *";

    private final LoanService loanService;
    private final EmailService emailService;

    @Value("${application.mail.message.loan.overdue}")
    private String message;

    //@Scheduled(cron=CRON_LATE_LOANS_SEGUNDOS)
    @Scheduled(cron=CRON_LATE_LOANS)
    public void sendMailToLateLoans(){
        List<Optional<Loan>> allOverdueLoans = loanService.getAllOverdueLoans(3);
        List<String> emailFromCustomersWithLoanOverdue = allOverdueLoans.stream().map(optionalLoan -> {
            Loan loan = optionalLoan.get();
            String email = loan.getCustomerEmail();
            return email;
        }).collect(Collectors.toList());

        emailService.sendEmailToLoansOverdue(message, emailFromCustomersWithLoanOverdue);
    }
}
