package com.aprendendotddspring.aprendendo.api.resource;

import com.aprendendotddspring.aprendendo.api.ApplicationControllerAdvice;
import com.aprendendotddspring.aprendendo.api.dto.BooktDTO;
import com.aprendendotddspring.aprendendo.api.exceptions.BusinessException;
import com.aprendendotddspring.aprendendo.config.Beans;
import com.aprendendotddspring.aprendendo.entity.Book;
import com.aprendendotddspring.aprendendo.model.repository.LoanRepostory;
import com.aprendendotddspring.aprendendo.service.BookService;

import com.aprendendotddspring.aprendendo.service.LoanService;
import com.aprendendotddspring.aprendendo.service.impl.LoanServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//configurações base
@ExtendWith(SpringExtension.class)//extends springExtension
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
        BookController.class,
        Beans.class,
        ApplicationControllerAdvice.class
})
public class BookControllerTest {

    //primeiro passo é definir a rota que iriar ser trabalhada
    static final String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    //criar instancias mockadas para ser usadas em nosso contexto
    @MockBean
    private BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() throws Exception{

        //cenario

        BooktDTO dto = createNewBook();

        Book savedBook = createValidBook();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //verificação e execução
        mvc
                .perform(request)
                .andExpect( status().isCreated() )
                .andExpect( jsonPath("id").value(dto.getId()))
                .andExpect( jsonPath("title").value(dto.getTitle()))
                .andExpect( jsonPath("author").value(dto.getAuthor()))
                .andExpect( jsonPath("isbn").value(dto.getIsbn()));

    }

    private Book createValidBook() {
        return Book.builder()
                .id(1L)
                .author("Vim")
                .title("terminal")
                .isbn("321")
                .build();
    }

    private BooktDTO createNewBook() {
        return new BooktDTO()
                .builder()
                .id(1L)
                .author("Vim")
                .title("terminal")
                .isbn("321")
                .build();
    }

    @Test
    @DisplayName("Deve lançar um erro de validação quando não houver dados suficientes")
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BooktDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", hasSize(3)) );

    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar livro com ISBN já cadastrado")
    public void createBookWithDuplicatedIsbn() throws Exception{

        BooktDTO dto = createNewBook();

        String json = new ObjectMapper().writeValueAsString(dto);
        String mensagemErro = "ISBN já cadastrado";

        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(mensagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));
    }

    @Test
    @DisplayName("Deve exibir os detalhes de um livro")
    public void getBookDetailTest() throws Exception {
        //cenario
        Long id = 11L;
        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();
        BDDMockito.given(service.getById(id))
                .willReturn(Optional.of(book));

        //Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()));

    }

    @Test
    @DisplayName("Deve retornar not found quando o livro procurado não existir")
    public void bookNotFountTest() throws Exception {
        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void shouldDeleteABook() throws Exception {
//       request for API/BOOKS/:id
        //cenario
        Long id = 1L;

        BDDMockito.given(service.getById(anyLong()))
                .willReturn(Optional.of(Book.builder().id(id).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve lançar um not found quando não encontrar o livro para deletar")
    public void deleteBookError() throws Exception{
        BDDMockito.given(service.getById(anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve alterar um livro")
    public void updateBookTest() throws Exception {
        Long id = 11L;
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book updatingBook = Book.builder()
                .id(id)
                .author("Arduino")
                .title("Jairson mendes")
                .isbn("321")
                .build();
        BDDMockito.given(service.getById(anyLong()))
                .willReturn(Optional.of(updatingBook));

        Book updatedBook = Book.builder()
                .id(1L)
                .author("Vim")
                .title("terminal")
                .isbn("321")
                .build();
        BDDMockito.given(service.update(updatingBook))
                .willReturn(updatingBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(updatingBook.getId()))
                .andExpect(jsonPath("author").value(updatingBook.getAuthor()))
                .andExpect(jsonPath("title").value(updatingBook.getTitle()))
                .andExpect(jsonPath("isbn").value("321"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar alterar um livro inexistente")
    public void errorBookUpdatingTest() throws Exception{

        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given(service.getById(anyLong()))
                .willReturn(Optional.empty());


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 11))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooktest() throws Exception {

        Long id  = 11L;

        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given( service.find(Mockito.any(Book.class), Mockito.any(Pageable.class) ) )
                .willReturn( new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0,100), 1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(),
                book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

}
