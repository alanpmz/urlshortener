package com.alanpmz.urlshortener.dto;

import com.alanpmz.urlshortener.model.Url;

import java.time.LocalDateTime;

public record UrlResponse(String originalUrl,
                          String shortCode,
                          LocalDateTime createdAt,
                          LocalDateTime expiresAt,
                          Long clickCount,
                          Boolean active) {


    public static UrlResponse fromEntity(Url url){
        return new UrlResponse(
                url.getOriginalUrl(),
                url.getShortCode(),
                url.getCreatedAt(),
                url.getExpiresAt(),
                url.getClickCount(),
                url.getActive()
        );
    }
}
