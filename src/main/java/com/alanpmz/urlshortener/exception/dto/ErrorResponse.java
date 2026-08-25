package com.alanpmz.urlshortener.exception.dto;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp,
                            Integer status,
                            String error,
                            String path) {

    public static ErrorResponse build(Integer status, Exception ex, String path){
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                ex.getMessage(),
                path);
    }

    public static ErrorResponse build(Integer status, String message, String path){
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                message,
                path);
    }

}
