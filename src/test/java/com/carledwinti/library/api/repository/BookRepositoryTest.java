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

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest//Mockando um bando de dados em memory --> indica que farei testes com JPA, ele irá criar uma instancia do
// banco de dados em memory(H2 é necessário para essa execução), somente para os testes e ao final dos testes irá apagar tudo
public class BookRepositoryTest {

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
        Book bookPersist = testEntityManager.persist(Book.builder().author(derikAuthor).title(derikTitle).isbn("456").build());

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

}
