package com.aprendendotddspring.aprendendo.api.exceptions;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

    private List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();

        bindingResult.getAllErrors().stream()
                .forEach(error -> this.errors.add(
                        error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException ex) {
        this.errors = Arrays.asList(ex.getMessage());
    }

    public ApiErrors(ResponseStatusException ex) {
        this.errors = Arrays.asList(ex.getReason());
    }

    public ApiErrors(List<ObjectError> allErrors) {
        this.errors = Arrays.asList(allErrors.toString());

    }

    public List<String> getErrors(){
        return errors;
    }
}
