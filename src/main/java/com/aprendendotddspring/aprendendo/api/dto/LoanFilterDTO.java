package com.aprendendotddspring.aprendendo.api.dto;

import com.aprendendotddspring.aprendendo.entity.Loan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanFilterDTO{

    private String isbn;
    private String customer;
}
