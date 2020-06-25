package com.aprendendotddspring.aprendendo.model.repository;

import com.aprendendotddspring.aprendendo.entity.Book;
import com.aprendendotddspring.aprendendo.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    LoanRepostory repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("deve verificar se existe emprestimo não devolvido para o livro")
    public void existsbyBookAndNotReturnedTest(){

        Book book = createNewBook("123");
        entityManager.persist(book);

        Loan loan = Loan.builder()
                .book(book)
                .customer("Fuçano")
                .loanDate(LocalDate.now())
                .build();

        entityManager.persist(loan);

        //execução
        boolean exists = repository.existsByBookAndNotReturned(book);

        //verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("deve obter um emprestimo pelo isbn do livro ou pelo customer")
    public void findByBookIsbnOrCustumerTest(){
        Loan loanAndBook = createAndPersistLoan(LocalDate.now());

        Page<Loan> result = repository.findByBookIsbnOrCustumer("123", "Fulano", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loanAndBook);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve obter emprestimo cuja data emprestimo for maior ou igual" +
            "a tres dias atras e não returnar")
    public void findBtLoanDateLessAndNotReturnedTest(){
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = repository.findByLoansDatesLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).hasSize(1).contains(loan);


    }

    @Test
    @DisplayName("Deve obter emprestimo cuja data emprestimo for maior ou igual" +
            "a tres dias atras e não returnar")
    public void notfindBytLoanDateLessAndNotReturnedTest(){
        Loan loan = createAndPersistLoan(LocalDate.now());

        List<Loan> result = repository.findByLoansDatesLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).isEmpty();


    }


    public Loan createAndPersistLoan(LocalDate localDate){
        Book book = createNewBook("123");
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(localDate).build();
        entityManager.persist(loan);

        return loan;
    }



    private Book createNewBook(String isbn) {
        return Book
                .builder()
                .title("As aventuras")
                .author("beltrano")
                .isbn(isbn)
                .build();
    }
}
