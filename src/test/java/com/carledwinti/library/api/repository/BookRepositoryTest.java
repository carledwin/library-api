package com.carledwinti.library.api.repository;

import com.carledwinti.library.api.model.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest//Mockando um bando de dados em memory --> indica que farei testes com JPA, ele irá criar uma instancia do
// banco de dados em memory(H2 é necessário para essa execução), somente para os testes e ao final dos testes irá apagar tudo
public class BookRepositoryTest {

    /** ********* TESTES DE INTEGRAÇÃO COM O BANCO DE DADOS IN MEMORY H2 ************************ */

    // Gerencia uma conexao com o bando de dados Mockado em memory -->
    // este objeto precisa ser injetado no context para criarmos os scenario, ele simula um entityManager, porém para testes
    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnsTrueWhenISBNExists(){
        //scenario
        String isbn = "123";
        String derikTitle = "Lua Nova";
        String derikAuthor = "Derik";

        //executando efetivamente inclusão de dados no banco de dados sem mock
        Book book = bookRepository.save(Book.builder().author("Lane").title("Arredores da Ilha").isbn(isbn).build());
        //ou
        Book bookPersist = testEntityManager.persist(createNewBook());

        //execution - executando efetivamente consulta no banco de dados sem mock
        boolean exists = bookRepository.existsByIsbn(isbn);

        /*log da execution
        Hibernate: insert into book (id, author, isbn, title) values (null, ?, ?, ?)
        Hibernate: insert into book (id, author, isbn, title) values (null, ?, ?, ?)
        Hibernate: select book0_.id as col_0_0_ from book book0_ where book0_.isbn=? limit ?*/

        //verification
        //testando persistencia com bookRepository
        Assertions.assertThat(exists).isTrue();
        //testando persistencia com testEntityManager
        Assertions.assertThat(bookPersist).isNotNull();
        Assertions.assertThat(bookPersist.getId()).isNotNull();
        Assertions.assertThat(bookPersist.getTitle()).isEqualTo(derikTitle);
        Assertions.assertThat(bookPersist.getAuthor()).isEqualTo(derikAuthor);
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o isbn informado")
    public void returnsFalseWhenISBNDoesntExists(){
        //scenario

        //execution
        boolean exists = bookRepository.existsByIsbn("456");

        //verification
        Assertions.assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void findById(){
        //scenario
        Book book = createNewBook();

        //execution 1
        Book persistedBook = testEntityManager.persist(book);

        //execution 2
        Optional<Book> foundOptionalBook = bookRepository.findById(persistedBook.getId());

        Assertions.assertThat(foundOptionalBook.isPresent()).isTrue();
        Assertions.assertThat(foundOptionalBook.get().getId()).isEqualTo(persistedBook.getId());
        Assertions.assertThat(foundOptionalBook.get().getAuthor()).isEqualTo(persistedBook.getAuthor());
        Assertions.assertThat(foundOptionalBook.get().getTitle()).isEqualTo(persistedBook.getTitle());
        Assertions.assertThat(foundOptionalBook.get().getIsbn()).isEqualTo(persistedBook.getIsbn());
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void save(){
        Book book = createNewBook();
        Book savedBook = bookRepository.save(book);

        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getAuthor()).isEqualTo(createNewBook().getAuthor());
        Assertions.assertThat(savedBook.getTitle()).isEqualTo(createNewBook().getTitle());
        Assertions.assertThat(savedBook.getIsbn()).isEqualTo(createNewBook().getIsbn());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void delete(){
        //scenario
        Book book = createNewBook();

        //execution 1
        Book savedBook = bookRepository.save(book);
        Book foundBook = testEntityManager.find(Book.class, savedBook.getId());

        //verification 1
        Assertions.assertThat(foundBook).isNotNull();
        Assertions.assertThat(foundBook.getAuthor()).isEqualTo(createNewBook().getAuthor());
        Assertions.assertThat(foundBook.getTitle()).isEqualTo(createNewBook().getTitle());
        Assertions.assertThat(foundBook.getIsbn()).isEqualTo(createNewBook().getIsbn());

        //main execution and verification
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> bookRepository.delete(savedBook));

        //verification
        Book notFoundBook = testEntityManager.find(Book.class, savedBook.getId());
        Assertions.assertThat(notFoundBook).isNull();
    }

    private Book createNewBook() {
        return Book.builder().author("Derik").title("Lua Nova").isbn("456").build();
    }
}
