package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.dto.BookDTO;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(SpringExtension.class)//JUnit 5! //Cria um mini context para executar os testes
@ActiveProfiles("test")//Define o ambiente/profile/perfil de 'test' para a execução destes testes
@WebMvcTest//Para criar context Rest e testar os métodos da api
@AutoConfigureMockMvc //configura um objeto para podermos realizar as requisições e permitirá a utilização do @Autowired
public class BookControllerTest {

    static String URL_BOOK_API = "/api/books";

    @Autowired
    MockMvc mockMvc;//responsável por mockar as requisições para a api

    @MockBean //mockBean é um mock especializado para criar uma instancia mock de um service para ser utilizado dentro do context do test e que pode ter o comportamento modificado de acordo com a necessida do test
    BookService bookService;

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception {

        //scenario *****************************************************************************************************
        BookDTO bookDTO = BookDTO.builder()
                            .author("Artur")
                            .title("As Aventuras do Rei")
                            .isbn("123456")
                        .build();
        String bookDTOJson = new ObjectMapper().writeValueAsString(bookDTO);

        //mock *********************************************************************************************************
        Book savedBook = Book.builder()
                .author("Artur")
                .title("As Aventuras do Rei")
                .isbn("123456")
                .id(12l)
                .build();
        BDDMockito.given((bookService.save(Mockito.any(Book.class)))).willReturn(savedBook);

        //define uma requisição
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(URL_BOOK_API)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(bookDTOJson);

        //execution ****************************************************************************************************
        //verification
        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(12l))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(bookDTO.getIsbn()));
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para a criação do livro.")
    public void createInvalidBookTest() throws Exception {

        //scenario
        //caso seja enviado um objeto com todas as propriedades vazias irá lançar
        //org.springframework.web.util.NestedServletException: Request processing failed;
        // nested exception is java.lang.IllegalArgumentException: source cannot be null, isso porque o
        //modelMapper não aceitará um objeto com propriedades 'todas' vazias
        //Será necessario anotar o @RequestBody com @Valid e anotar alguns campos com validations do
        //javax.validation ex.:javax.validation.constraints.NotEmpty;
        //Desta forma o Spring não permitirá que a requisição aconteça
        String bookDTOJson = new ObjectMapper().writeValueAsString(new BookDTO());

        //mock
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(URL_BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bookDTOJson);

        //execution
        //aqui vamos utilizar o ExceptionHandler para tratar exceptions da nossa api
        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(3)));
    }



}
