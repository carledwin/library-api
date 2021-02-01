package com.carledwinti.library.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @NotEmpty
    private String isbn;
    @NotEmpty
    private String customer;
    @Column(name="customer_email")
    @NotEmpty
    private String customerEmail;
    @JoinColumn(name="id_book")
    @ManyToOne
    private Book book;
    @NotNull
    private LocalDate loanDate;
    private Boolean returned;
}
