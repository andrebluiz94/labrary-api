package com.aprendendotddspring.aprendendo.api.resource;

import com.aprendendotddspring.aprendendo.api.dto.BooktDTO;
import com.aprendendotddspring.aprendendo.api.dto.LoanDTO;
import com.aprendendotddspring.aprendendo.api.dto.LoanFilterDTO;
import com.aprendendotddspring.aprendendo.api.dto.ReturnedLoanDTO;
import com.aprendendotddspring.aprendendo.entity.Book;
import com.aprendendotddspring.aprendendo.entity.Loan;
import com.aprendendotddspring.aprendendo.service.BookService;
import com.aprendendotddspring.aprendendo.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {


    private final LoanService service;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto){

        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,"Book not found for passed isbn"));
        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = service.save(entity);

        return entity.getId();
    }


    @PatchMapping("{id}")
    public void returnBook(
            @PathVariable Long id,
            @RequestBody ReturnedLoanDTO dto){

        Loan loan = service.getById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.isReturned());
        service.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanDTO dto, Pageable pageRequest){
        LoanFilterDTO filter = modelMapper.map(dto, LoanFilterDTO.class);
        Page<Loan> result = service.find(filter, pageRequest);
        List<LoanDTO> list = result.getContent().stream()
                .map(entity -> {
                    Book book = entity.getBook();
                    BooktDTO booktDTO = modelMapper.map(book, BooktDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(booktDTO);
                    return loanDTO;
                })
                .collect(Collectors.toList());

        return new PageImpl<LoanDTO>(list, pageRequest, result.getTotalElements());
    }

}
