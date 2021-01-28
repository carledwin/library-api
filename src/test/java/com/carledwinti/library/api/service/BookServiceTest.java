package com.carledwinti.library.api.service;

import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.repository.BookRepository;
import com.carledwinti.library.api.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

//Esta classe diferente da classe de teste para o Controller precisa somente das @s abaixo
@ExtendWith(SpringExtension.class)//sobe o context para os testes
@ActiveProfiles("test")//define que iremos trabalhar no context de test
public class BookServiceTest {

    //Caso não exista nenhuma implementation de BookService ao executar
    // receberemos java.lang.NullPointerException
    BookService bookService;

    //O Spring já possui uma implementação do JpaRepository, caso
    //não anotemos o bookRepository aqui com @MockBean(analogo ao @Autowired só que o nosso é para MOCKAR
    // ele dará java.lang.NullPointerException
    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp(){
        this.bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //scenario
        Book book = Book.builder()
                .isbn("123")
                .author("Blano")
                .title("A vida de Sararhik")
                .build();

        //mock
        //o bookService retornará o que o bookRepository mockado com @MockBean
        // retorna como padrão que são os valores padrões do objeto Book desta forma precisaremos mockar o retorno
        Book bookMock = Book.builder()
                .id(13l)
                .title("A vida de Sararhik")
                .isbn("123")
                .author("Blano")
                .build();
        Mockito.when(bookRepository.save(book)).thenReturn(bookMock);

        //execution
        Book savedBook = bookService.save(book);

        //verification
        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getIsbn()).isEqualTo("123");
        Assertions.assertThat(savedBook.getTitle()).isEqualTo("A vida de Sararhik");
        Assertions.assertThat(savedBook.getAuthor()).isEqualTo("Blano");
    }
}
