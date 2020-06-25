package com.aprendendotddspring.aprendendo.api.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String mensagemErro) {
        super(mensagemErro);
    }
}
