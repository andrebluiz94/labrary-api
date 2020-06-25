package com.aprendendotddspring.aprendendo.api.resource;

import com.aprendendotddspring.aprendendo.api.ApplicationControllerAdvice;
import com.aprendendotddspring.aprendendo.api.dto.LoanDTO;

import com.aprendendotddspring.aprendendo.api.dto.LoanFilterDTO;
import com.aprendendotddspring.aprendendo.api.dto.ReturnedLoanDTO;
import com.aprendendotddspring.aprendendo.api.exceptions.BusinessException;
import com.aprendendotddspring.aprendendo.config.Beans;
import com.aprendendotddspring.aprendendo.entity.Book;
import com.aprendendotddspring.aprendendo.entity.Loan;
import com.aprendendotddspring.aprendendo.service.BookService;
import com.aprendendotddspring.aprendendo.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("Test")
@WebMvcTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
        Beans.class,
        BookController.class,
        ApplicationControllerAdvice.class

})
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;


    @Test
    @DisplayName("Deve realizar um emprestimo")
    public void createLoanTest() throws Exception {

        LoanDTO dto = getCreateLoanDTO("Beltrano");
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(11L).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(1L).customer("Beltrano").book(book).loanDate(LocalDate.now()).build();
        BDDMockito.given(loanService.save(any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro inexistente")
    public void invalidIsbnLoanTest() throws  Exception{
        //cenario
        LoanDTO dto = getCreateLoanDTO("Fulano");
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //verificação e execução
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));
    }

    private LoanDTO getCreateLoanDTO(String fulano) {
        return LoanDTO.builder().isbn("123").customer(fulano).email("customer@email.com").build();
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer esmprestimo de um livro emprestado.")
    public void loanedBookErrorOneCreateLoanTest() throws Exception{

        LoanDTO dto = getCreateLoanDTO("Beltrano");
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(11L).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn(dto.getIsbn())).willReturn(Optional.of(book));

        BDDMockito.given(loanService.save(any()))
                .willThrow(new BusinessException("Book already loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));

    }

    @Test
    @DisplayName("Deve retornar um livro")
    public void returnBookTest() throws Exception{
        //cenario
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Loan loan = Loan.builder().id(11L).build();
        BDDMockito.given(loanService.getById(any())).willReturn(Optional.of(loan));
        mvc.perform(
            patch(LOAN_API.concat("/1"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
        ).andExpect(status().isOk());

        verify(loanService, times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retonar 404 quando tentar devolver um livro inexistente.")
    public void returnedInexistentBook() throws Exception{

        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(false).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(any())).willReturn(Optional.empty());

        mvc.perform(
                patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve perquisar emprestimos")
    public void findLoanTest() throws Exception{
        Long id = 1l;

        Loan loan = Loan.builder()
                .loanDate(LocalDate.now())
                .customer("Beltrano")
                .build();
        loan.setId(id);
        Book book = Book.builder().id(id).isbn("321").build();
        loan.setBook(book);

        BDDMockito.given( loanService.find(any(LoanFilterDTO.class), any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan),
                        PageRequest.of(0,10),1));

        String querystring = String.format("?isbn=%s&customer=%s&page=0&size=100",
                book.getIsbn(), loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(querystring))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform( request )
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100));



    }
}
