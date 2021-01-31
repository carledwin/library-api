package com.carledwinti.library.api.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/*@Getter
@Setter*/
@Data
@Builder//cria um builder para o DTO para ser utilizado em outras classes
@NoArgsConstructor //cria um construtor padrão
@AllArgsConstructor //cria um contructor com todos os argumentos
public class BookDTO {
    private Long id;
    @NotEmpty
    private String title;
    @NotEmpty
    private String author;
    @NotEmpty
    private String isbn;
    private List<LoanDTO> loanDTOs;
}
