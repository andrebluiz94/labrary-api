package com.aprendendotddspring.aprendendo.api.resource;

import com.aprendendotddspring.aprendendo.api.dto.BooktDTO;
import com.aprendendotddspring.aprendendo.api.dto.LoanDTO;
import com.aprendendotddspring.aprendendo.api.exceptions.ApiErrors;
import com.aprendendotddspring.aprendendo.api.exceptions.BusinessException;
import com.aprendendotddspring.aprendendo.entity.Book;
import com.aprendendotddspring.aprendendo.entity.Loan;
import com.aprendendotddspring.aprendendo.service.BookService;
import com.aprendendotddspring.aprendendo.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
@Slf4j
public class BookController {


    private final ModelMapper modelMapper;
    private final BookService service;
    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Registra um livro")
    public BooktDTO create(@RequestBody @Valid  BooktDTO dto){
        log.info("creatting a book for isbn: {}", dto.getIsbn());

        Book entity = modelMapper.map(dto, Book.class);

        entity = service.save(entity);

        return modelMapper.map(entity, BooktDTO.class);
    }

    @GetMapping("{id}")
    @ApiOperation("Obtem um livro details by id")
    public BooktDTO get(@PathVariable Long id){
        log.info("get book of id: {}", id);

        return service.getById(id)
                .map( book ->  modelMapper.map(book, BooktDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Deleta um livro")
    public void delete(@PathVariable Long id){
        log.info("delete book of 1: {}", id);
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);

    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public BooktDTO update(@PathVariable Long id, @Valid @RequestBody BooktDTO dto){

        return service.getById(id).map(book ->{
            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());
            book.setIsbn(dto.getIsbn());

            book = service.update(book);

            return modelMapper.map(book, BooktDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BooktDTO> find(BooktDTO dto, Pageable pageRequest){
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);
        List<BooktDTO> list = result.getContent().stream()
                .map(entity -> modelMapper.map(entity, BooktDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<BooktDTO>(list, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> loanByBook(@PathVariable Long id, Pageable pageable){
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<Loan> result = loanService.getLoansByBook(book, pageable);
        List<LoanDTO> list = result.getContent()
                .stream().map(
                        loan -> {
                            Book loanBook = loan.getBook();
                            BooktDTO bookDTO = modelMapper.map(loanBook, BooktDTO.class);
                            LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                            loanDTO.setBook(bookDTO);
                            return loanDTO;
                        }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
    }

}
