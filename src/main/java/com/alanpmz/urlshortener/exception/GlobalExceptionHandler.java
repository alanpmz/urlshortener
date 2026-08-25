package com.alanpmz.urlshortener.exception;

import com.alanpmz.urlshortener.exception.dto.ErrorResponse;
import com.alanpmz.urlshortener.exception.dto.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // business exceptions

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotFoundException(
            UrlNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.build(
                404,
                ex,
                request.getRequestURI()
        );

        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUrlExpiredException(
            UrlExpiredException ex,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.build(
                410,
                ex,
                request.getRequestURI()
        );

        return ResponseEntity.status(410).body(response);
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrlException(
            InvalidUrlException ex,
            HttpServletRequest request){

        ErrorResponse response = ErrorResponse.build(
                400,
                ex,
                request.getRequestURI()
        );

        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ResponseEntity<ErrorResponse> handleShortCodeGenerationException(
            ShortCodeGenerationException ex,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.build(
                409,
                ex,
                request.getRequestURI()
        );

        return ResponseEntity.status(409).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request){

        ValidationErrorResponse response = ValidationErrorResponse.build(
                ex,
                request.getRequestURI()
        );

        return ResponseEntity.status(400).body(response);
    }

    // bad requests

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.build(
                404,
                ex,
                request.getRequestURI()
        );

        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request){

        ErrorResponse response = ErrorResponse.build(
                400,
                ErrorMessage.INVALID_REQUEST_BODY.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(400).body(response);
    }




    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.build(
                400,
                "Invalid value '%s' for parameter '%s'"
                        .formatted(ex.getValue(), ex.getName()),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(
            PropertyReferenceException ex,
            HttpServletRequest request){

        ErrorResponse response =  ErrorResponse.build(
                400,
                "Invalid value '%s'".formatted(ex.getPropertyName()),
                request.getRequestURI()
        );

        return ResponseEntity.status(400).body(response);
    }

}