package com.aprendendotddspring.aprendendo.service;

import com.aprendendotddspring.aprendendo.api.exceptions.BusinessException;
import com.aprendendotddspring.aprendendo.entity.Book;
import com.aprendendotddspring.aprendendo.model.repository.BookRepository;
import com.aprendendotddspring.aprendendo.service.impl.BookServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){

        //cenario
        Book book = createValidBook();

        when( repository.save(book) ).thenReturn(Book
                .builder()
                .author("Eu")
                .isbn("123")
                .title("Titulo")
                .id(11L)
                .build());

        //execução
        Book savedBook = service.save(book);

        //verificacao
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
        assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
        assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());

    }

    private Book createValidBook() {
        return Book
                .builder()
                .isbn("123")
                .author("Eu")
                .title("Titulo")
                .build();
    }

    @Test
    @DisplayName("Deve lançar um error de negocio ao tentar salvar livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN(){
        //cenario
        Book book = createValidBook();
        when(repository.existsByIsbn(Mockito.any())).thenReturn(true);

        //execução
        Throwable excption = Assertions.catchThrowable(() -> service.save(book));

        //verificações
        assertThat(excption)
                .isInstanceOf(BusinessException.class)
                .hasMessage("ISBN já cadastrado");
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void getByIdTest(){
        //cenario
        Long id = 1L;

        Book book = createValidBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        //execução
        Optional<Book> foundBook = service.getById(id);

        //veririficações
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obeter um livro pelo id")
    public void bookNotFound(){
        //cenario
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        //execução
        Optional<Book> notFoundBook = service.getById(id);

        //verificação
        assertThat(notFoundBook).isEmpty();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookById(){
        //cenario

        Book book = Book.builder().id(11L).build();
        //execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->service.delete(book));

        //verirication
        Mockito.verify(repository, Mockito.times(1)).delete(book);

    }

    @Test
    @DisplayName("Deve lançar um erro ao passar livro vazio")
    public void erroAoDeletarLivro(){
        //cenario
        Book book = new Book();
        //execução
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, ()-> service.delete(book));

        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve alterar um livro")
    public void updateInvalidBookTest() throws Exception{
        //cenario
        Book book = new Book();
        //execução
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));
        //verificação
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateSucessoBookTest(){
        //cenario
        Book book = Book
                .builder()
                .id(1L)
                .title("um nome fim")
                .author("eu")
                .isbn("321")
                .build();
        //execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(()->service.update(book));
        //verificação
        Mockito.verify(repository, Mockito.times(1)).save(book);
    }

    @Test
    @DisplayName("Tentar atualizar essa disgraça")
    public void updateBook() throws Exception {
        //cenario
        Long id = 11L;

        Book updatingBook = Book.builder().id(id).build();

        //simulação
        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        when(repository.save(updatingBook)).thenReturn(updatedBook);

        //Execução
        Book book = service.update(updatedBook);
        System.out.println(book);
        //verificação
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void filterBookTest(){
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(list, pageRequest, 1);
        when(repository
                .findAll(
                        Mockito.any(Example.class),
                        Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> result = service.find(book, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro pelo ISBN")
    public void getBookByIsbn(){
        //cenario
        String isbn = "1230";
        when(repository.findByIsbn(isbn))
                .thenReturn(Optional.of(
                        Book
                                .builder()
                                .id(11L)
                                .isbn(isbn)
                                .build()));
        //execução
        Optional<Book> book = service.getBookByIsbn(isbn);
        //verificação
        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(11L);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);

    }
}
