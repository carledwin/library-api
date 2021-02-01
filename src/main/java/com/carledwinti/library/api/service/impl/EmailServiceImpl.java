package com.carledwinti.library.api.service.impl;

import com.carledwinti.library.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${application.mail.default.sender}")
    private String sender;

    @Override
    public void sendEmailToLoansOverdue(String message, List<String> emailFromCustomersWithLoanOverdue) {
        String[] mails = emailFromCustomersWithLoanOverdue.toArray(new String[emailFromCustomersWithLoanOverdue.size()]);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(sender);
        simpleMailMessage.setSubject("Livro com empréstimo em atraso");
        simpleMailMessage.setText(message);
        simpleMailMessage.setTo(mails);
        javaMailSender.send(simpleMailMessage);
    }
}
