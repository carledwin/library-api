package com.carledwinti.library.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Loan {

    //caso não seja incluída a @nnotation @Id ao tentar executar testes ocorrerá a falha --> No identifier specified
    // for entity: com.carledwinti.library.api.model.Loan
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String isbn;
    private String customer;
    @Column(name="customer_email")
    private String customerEmail;
    @JoinColumn(name="id_book")
    @ManyToOne
    private Book book;
    private LocalDate loanDate;
    private Boolean returned;
}
