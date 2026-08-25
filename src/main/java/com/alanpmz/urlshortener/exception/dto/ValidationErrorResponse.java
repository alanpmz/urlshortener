package com.alanpmz.urlshortener.exception.dto;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(LocalDateTime timestamp,
                                      Integer status,
                                      List<FieldErrorDto> errors,
                                      String path) {

    public static ValidationErrorResponse build(MethodArgumentNotValidException ex, String path){

        List<FieldErrorDto> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(error -> new FieldErrorDto(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        return new ValidationErrorResponse(LocalDateTime.now(), ex.getStatusCode().value(), errors, path);
    }

}

record FieldErrorDto(String field, String message){}
