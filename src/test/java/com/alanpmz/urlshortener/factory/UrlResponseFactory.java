package com.alanpmz.urlshortener.factory;

import com.alanpmz.urlshortener.dto.UrlResponse;

import java.time.LocalDateTime;

public class UrlResponseFactory {

    public static UrlResponse build(){
        return new UrlResponse("https://google.com",
                "abc123",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                0L,
                true);
    }

    public static UrlResponse buildWithUrl(String url){
        return new UrlResponse(url,
                "abc123",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                0L,
                true);
    }

    public static UrlResponse buildWithUrlAndShortCode(String url, String shortCode){
        return new UrlResponse(url,
                shortCode,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                0L,
                true);
    }

    public static UrlResponse buildWithUpdate(LocalDateTime expiresAt, boolean active){
        return new UrlResponse("https://google.com",
                "abc123",
                LocalDateTime.now(),
                expiresAt != null ? expiresAt : LocalDateTime.now().plusDays(7),
                0L,
                active);
    }
}
