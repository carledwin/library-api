package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.dto.BookDTO;
import com.carledwinti.library.api.exception.BusinessException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Optional;

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


    //validação de integridade do objeto enviado na request
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

    //validação de regra de negócio
    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um livro com isbn já utilizado por outro.")
    public void createBookWithDuplicatedIsbn() throws Exception {
        //scenario
        BookDTO bookDTO = createNewBookDTO();
        String bookDTOJson = new ObjectMapper().writeValueAsString(bookDTO);

        //mock

        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willThrow(new BusinessException(ConstantsError.MSG_ERROR_ISBN_ALREADY_EXISTS));
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(URL_BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bookDTOJson);

        //execution
        //verification
        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value(ConstantsError.MSG_ERROR_ISBN_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetails() throws Exception {
        //scenario
        Long id = createNewBook().getId();
        Book book = createNewBook();

        //mock da ** camada service **
        BDDMockito.given(bookService.getByid(id)).willReturn(Optional.of(book));

        //execution(wheb)
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilders.get(URL_BOOK_API+"/"+id)
                        .accept(MediaType.APPLICATION_JSON);

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
        .andExpect(MockMvcResultMatchers.jsonPath("title").value(createNewBookDTO().getTitle()))
        .andExpect(MockMvcResultMatchers.jsonPath("author").value(createNewBookDTO().getAuthor()))
        .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(createNewBookDTO().getIsbn()));

    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    public void bookNotFound() throws Exception {
        //scenario
        Long id = createNewBook().getId();

        //mock
        BDDMockito.given(bookService.getByid(Mockito.anyLong())).willReturn(Optional.empty());

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilders.get(URL_BOOK_API+"/"+id)
                        .accept(MediaType.APPLICATION_JSON);

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBook() throws Exception {
        //scenario
        Book book = Book.builder().id(56l).build();
        Long id = book.getId();

        //mock
        BDDMockito.given(bookService.getByid(Mockito.anyLong())).willReturn(Optional.of(book));
        //BDDMockito.given(bookService.delete(Mockito.anyLong()));

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(URL_BOOK_API+"/"+id);
        //caso nao tenha sido implementado o method DELETE --> java.lang.AssertionError: Status expected:<204> but was:<405>

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(MockMvcResultMatchers.status().isNoContent());
        Mockito.verify(bookService, Mockito.times(1)).getByid(id);
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar")
    public void deleteBookNotFound() throws Exception {
        //scenario
        Long id = 67l;

        //mock
        BDDMockito.given(bookService.getByid(Mockito.anyLong())).willReturn(Optional.empty());

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(URL_BOOK_API+"/"+id);

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(MockMvcResultMatchers.status().isNotFound());
        Mockito.verify(bookService, Mockito.times(1)).getByid(id);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBook() throws Exception {
        //scenario******************************************************************************************************
        Long id = 12l;
        String bookDTOJson = new ObjectMapper().writeValueAsString(createNewBookDTO());

        //mock**********************************************************************************************************
        Book existentBook = createNewBook();
        BDDMockito.given(bookService.getByid(id)).willReturn(Optional.of(existentBook));
        Book updatedBook = createNewBook();
        updatedBook.setAuthor("Hirzts");
        updatedBook.setTitle("Vagalume da Luz Azul");
        BDDMockito.given(bookService.update(existentBook)).willReturn(updatedBook);//caso o mock do update não seja declarado
        //será retornado do um erro org.springframework.web.util.NestedServletException: Request processing failed;
        // nested-aninhada exception is java.lang.IllegalArgumentException: source(MOCK do return do update) cannot be null

        //execution*****************************************************************************************************
        //caso nao tenha sido implementado retornará --> MockHttpServletResponse: Status = 405 Error message =
        // Request method 'PUT' not supported
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.put(URL_BOOK_API.concat("/"+id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bookDTOJson);

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(updatedBook.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(updatedBook.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(updatedBook.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(updatedBook.getIsbn()));

        /*Mockito.verify(bookService, Mockito.times(1)).getByid(id);
        Mockito.verify(bookService, Mockito.times(1)).update(updatedBook);*/
    }

    @Test
    @DisplayName("Deve retornar not found ao tentar atualizar um livro inexistente")
    public void updateBookNotFound() throws Exception {
        //scenario
        Long id = 12l;
        String bookDTOJson = new ObjectMapper().writeValueAsString(createNewBookDTO());

        //mock
        BDDMockito.given(bookService.getByid(Mockito.anyLong())).willReturn(Optional.empty());

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.put(URL_BOOK_API.concat("/"+id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bookDTOJson);

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @DisplayName("Deve filtar livros")
    public void findBookByFilters() throws Exception {
        //scenario
        Long id = 1l;
        Book book  = createNewBook();
        book.setId(id);

        //mock
        int page = 0, size = 100, total = 1;
        PageImpl<Book> bookPage = new PageImpl<>(Arrays.asList(book), PageRequest.of(page, size), total);
        BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class))).willReturn(bookPage);

        //execution
        //"/api/books?"
        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());
        //caso não esteja implementado o metodo no controller - java.lang.AssertionError: Status expected:<200> but was:<405>
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilders.get(URL_BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageNumber").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageSize").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1));
    }

    private BookDTO createNewBookDTO() {
        return BookDTO.builder().author("Andres").isbn("123").title("A mudança").build();
    }

    private Book createNewBook() {
        return Book.builder().id(12l).author("Andres").isbn("123").title("A mudança").build();
    }

}
