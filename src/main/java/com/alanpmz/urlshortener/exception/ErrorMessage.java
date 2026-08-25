package com.alanpmz.urlshortener.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    URL_NOT_FOUND("URL not found"),
    SHORT_CODE_GENERATION_ERROR("Short code couldn't be generated"),
    INVALID_URL("Invalid URL"),
    INVALID_REQUEST_BODY("Invalid request body"),
    URL_EXPIRED("URL has expired");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

}
