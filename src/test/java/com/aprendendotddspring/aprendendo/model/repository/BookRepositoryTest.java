package com.aprendendotddspring.aprendendo.model.repository;

import com.aprendendotddspring.aprendendo.entity.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Dve retornar verdadeiro quando existir " +
            "um livro na base com o isbn")
    public void returnTrueWhenIsbnExists(){

        //cenario
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        //execução
        boolean exists = repository.existsByIsbn(isbn);

        //verificação
        assertThat(exists).isTrue();
    }

    private Book createNewBook(String isbn) {
        return Book
                .builder()
                .title("As aventuras")
                .author("beltrano")
                .isbn(isbn)
                .build();
    }

    @Test
    @DisplayName("Deve retornar falso se o livro não existir" +
            " na base de dados com o isbn informado")
    public void returnFalseWhenIsbnDoesntExists(){
        //cenario
        String isbn = "123";

        //Execução
        boolean exist = repository.existsByIsbn(isbn);

        //verificacao
        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Deve obeter um livro por id")
    public void shouldBeReturnBookById(){
        //cenario
        Book book = createNewBook("321");
        entityManager.persist(book);

        //execução
        Optional<Book> foundBoot = repository.findById(book.getId());

        //verificação
        assertThat(foundBoot.isPresent()).isTrue();

    }

    @Test
    @DisplayName("Deve salvar  um livro")
    public void saveBookTest(){
        //cenario
        Book book = createNewBook("321");


        //execução
        Book savedBook = repository.save(book);

        //verificação
        assertThat(savedBook).isEqualTo(book);
        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro salvo")
    public void deleleBookTest(){
        //cenario
        Book book = createNewBook("321");
        entityManager.persist(book);

        //execução
        Book foundBook = entityManager.find(Book.class, book.getId());
        repository.delete(foundBook);

        //verificação
        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();

    }
}
