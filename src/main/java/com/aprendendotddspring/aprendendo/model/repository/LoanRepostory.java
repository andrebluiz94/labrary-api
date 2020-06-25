package com.aprendendotddspring.aprendendo.model.repository;

import com.aprendendotddspring.aprendendo.entity.Book;
import com.aprendendotddspring.aprendendo.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepostory extends JpaRepository<Loan, Long> {

    @Query(value = " select case when ( count(l.id) > 0 ) then true else false end from " +
            "Loan l where l.book = :book and (l.returned is null or l.returned is false) ")
    boolean existsByBookAndNotReturned(@Param("book") Book book);

    @Query(value = "select l from Loan as l join l.book as b where b.isbn = :isbn or l.customer =:customer")
    Page<Loan> findByBookIsbnOrCustumer(@Param("isbn") String isbn, @Param("customer") String custumer, Pageable pageable);

    Page<Loan> findByBook(Book book, Pageable pageable);

    @Query("select l from Loan l where l.loanDate <= :threeDayAgo and (l.returned is null or l.returned is false)")
    List<Loan> findByLoansDatesLessThanAndNotReturned(@Param("threeDayAgo") LocalDate threeDayAgo);
}
