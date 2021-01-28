package com.carledwinti.library.api.dto;

import lombok.*;

/*@Getter
@Setter*/
@Data
@Builder//cria um builder para o DTO para ser utilizado em outras classes
@NoArgsConstructor //cria um construtor padrão
@AllArgsConstructor //cria um contructor com todos os argumentos
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;


}
