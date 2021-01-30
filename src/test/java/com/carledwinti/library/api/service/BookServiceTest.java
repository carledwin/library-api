package com.carledwinti.library.api.service;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.exception.BusinessException;
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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//Esta classe diferente da classe de teste para o Controller precisa somente das @s abaixo
@ExtendWith(SpringExtension.class)//sobe o context para os testes
@ActiveProfiles("test")//define que iremos trabalhar no context de test
public class BookServiceTest {

    /** ******* ESTA CAMADA É DE TESTE UNITÁRIO COM MOCK SEM INTEGRAÇÃO(MOCKA) A CHAMADA AO REPOSITORY ************** */

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
        Book bookMock = createValidBook();
        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(bookRepository.save(book)).thenReturn(bookMock);

        //execution
        Book savedBook = bookService.save(book);

        //verification
        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getIsbn()).isEqualTo("123");
        Assertions.assertThat(savedBook.getTitle()).isEqualTo("A vida de Sararhik");
        Assertions.assertThat(savedBook.getAuthor()).isEqualTo("Blano");

        Mockito.verify(bookRepository, Mockito.times(1)).save(book);
    }

    @Test
    @DisplayName("Deve lançar erro de negocio ao tentar salvar livro com isbn duplicado")
    public void shoudNotSaveABookWithDuplicatedISBN(){
        //scenario
        Book book = createValidBook();
        //mock
        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(true);
        //execution
        Throwable throwable = Assertions.catchThrowable(() -> bookService.save(book));

        //verification
        Assertions.assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ConstantsError.MSG_ERROR_ISBN_ALREADY_EXISTS);//caso nao tenha cido implementada essa validação
        //pode retornar --> java.lang.AssertionError: Expecting actual not to be null

        //caso já exista pode ocorrer de mesmo assim ele chamar o save do repository, precisamos garantir que o
        //save não será executado/chamado
        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void getByid(){
        //scenario
        Long id = 123l;

        //mock
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(Book.builder().id(id).title("O Vagalume Azul").build()));

        //execution
        Optional<Book> foundOptionalBook = bookService.getByid(id);

        //verification
        Assertions.assertThat(foundOptionalBook.isPresent()).isTrue();
        Assertions.assertThat(foundOptionalBook.get().getId()).isEqualTo(123l);
        Assertions.assertThat(foundOptionalBook.get().getTitle()).isEqualTo("O Vagalume Azul");

        Mockito.verify(bookRepository, Mockito.times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve retornar vazio ao tentar obter um livro por Id quando ele não existe na base")
    public void bookNotFoundById(){
        //scenario
        Long id = 123l;
        //mock
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.empty());

        //execution
        Optional<Book> emptyOptionalBook = bookService.getByid(id);

        //verification
        Assertions.assertThat(emptyOptionalBook.isPresent()).isFalse();

        Mockito.verify(bookRepository, Mockito.times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve retornar exception ao tentar buscar livro com Id null.")
    public void idNullFindById(){
        //scenario
        Long id = null;

        //execution
        Throwable throwable = Assertions.catchThrowable(() -> bookService.getByid(id));

        //verification
        Assertions.assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ConstantsError.MSG_ERROR_ID_CANT_BE_NULL);

        //essa verificação é para garantir que o metodo delete nunca foi chamado já que não passou em uma
        // validação e lançou uma exception
        Mockito.verify(bookRepository, Mockito.never()).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBook(){
        //scenario
        Long id = 45l;
        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        Book updatingBook = Book.builder().id(id).build();

        //mock
        Mockito.when(bookRepository.save(updatingBook)).thenReturn(updatedBook);

        //execution 1
        Book book = bookService.update(updatingBook);

        //verification
        Assertions.assertThat(book.getId()).isEqualTo(updatedBook.getId());
        Assertions.assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        Assertions.assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        Assertions.assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());

        Mockito.verify(bookRepository, Mockito.times(1)).save(updatingBook);
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBook(){
        //scenario
        Long id = 102l;
        Book existentBook = createValidBook();
        existentBook.setId(id);
        //mock
        /** ***NÃO MOCKAMOS NADA NESTE CASO POIS O delete RETORNA void
         * ALTERNATIVA PARA ISSO É UTILIZAR O Mockito.verify PARA GARANTIR QUE O repository.delete() FOI INVOCADO
         * 1X PASSANDO EXATAMENTE ESTE LIVRO*** */

        //execution + verification(verifica que na execução não foi retornado nenhum erro)
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> bookService.delete(existentBook));

        //verification
        Mockito.verify(bookRepository, Mockito.times(1)).delete(existentBook);
        //Mockito.verify(bookRepository, Mockito.times(1)).delete(Book.builder().id(1233l).build());
        /** CASO VERIFIQUE QUE O LIVRO PASSADO PARA O DELETE É DIFERENTE DO VERIFICADO RETORNARÁ A FALHA:
         Argument(s) are different! Wanted:
         com.carledwinti.library.api.repository.BookRepository#0 bean.delete(
         Book(id=1233, title=null, author=null, isbn=null)
         );
         -> at com.carledwinti.library.api.service.BookServiceTest.deleteBook(BookServiceTest.java:177)
         Actual invocations have different arguments:
         com.carledwinti.library.api.repository.BookRepository#0 bean.delete(
         Book(id=102, title=A vida de Sararhik, author=Blano, isbn=123)
         );
         **/
    }

    @Test
    @DisplayName("Deve retornar exception ao tentar atualizar book com book ou id null.")
    public void bookIdNullUpdate(){
        //scenario
        Book book = null;
        //execution
        Throwable throwable = Assertions.catchThrowable(() -> bookService.update(book));

        //verification
        Assertions.assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ConstantsError.MSG_ERROR_BOOK_AND_ID_CANT_BE_NULL);

        //essa verificação é para garantir que o metodo save nunca foi chamado já que não passou em uma
        // validação e lançou uma exception
        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("** com Assertions AssertJ que valida a mensagem de retorno Deve retornar exception ao tentar deletar book com ou id null")
    public void idNullDeleteBook() {
        //scenario
        Book book = null;
        //exception
        Throwable throwable = Assertions.catchThrowable(() -> bookService.delete(book));

        //verification
        Assertions.assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ConstantsError.MSG_ERROR_BOOK_AND_ID_CANT_BE_NULL);

        //essa verificação é para garantir que o metodo delete nunca foi chamado já que não passou em uma
        // validação e lançou uma exception
        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName(" *** com Assertions JUnit Jupter Deve retornar exception ao tentar deletar book com ou id null")
    public void idNullDeleteBook2() {
        //scenario
        Book book = null;
        //exception
        IllegalArgumentException illegalArgumentException = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.delete(book));

        //verification
        Assertions.assertThat(illegalArgumentException.getLocalizedMessage()).isEqualTo(ConstantsError.MSG_ERROR_BOOK_AND_ID_CANT_BE_NULL);

        //essa verificação é para garantir que o metodo delete nunca foi chamado já que não passou em uma
        // validação e lançou uma exception
        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookByFilter(){
        //scenario
        Book bookFilter = Book.builder().id(13l).author("Vegan Unbras").isbn("123").title("Carros Velozes").build();
        int page=0, size=10,total=1;
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(bookFilter), pageRequest, total);
        List<Book> bookList = Arrays.asList(bookFilter);

        //mock
        Mockito.when(bookRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(bookPage);

        //execution
        Page<Book> foundBooksFilter = bookService.findByFilter(bookFilter, pageRequest);

        //verification
        Assertions.assertThat(foundBooksFilter).isNotNull();
        Assertions.assertThat(foundBooksFilter.isEmpty()).isFalse();
        Assertions.assertThat(foundBooksFilter.getContent()).isEqualTo(bookList);
        Assertions.assertThat(foundBooksFilter.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(foundBooksFilter.getPageable().getPageSize()).isEqualTo(10);
        Assertions.assertThat(foundBooksFilter.getTotalElements()).isEqualTo(1);
    }

    private Book createValidBook() {
        Book bookMock = Book.builder()
                .id(13l)
                .title("A vida de Sararhik")
                .isbn("123")
                .author("Blano")
                .build();
        return bookMock;
    }
}
