package com.alanpmz.urlshortener.utils;

import com.alanpmz.urlshortener.model.Url;

import java.time.LocalDateTime;

public class UrlTestBuilder {

    private Long id = 1L;
    private String originalUrl = "https://google.com";
    private String shortCode = "abc123";
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
    private Long clickCount = 0L;
    private Boolean active = true;

    public static UrlTestBuilder builder() {
        return new UrlTestBuilder();
    }

    public UrlTestBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public UrlTestBuilder originalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
        return this;
    }

    public UrlTestBuilder shortCode(String shortCode) {
        this.shortCode = shortCode;
        return this;
    }

    public UrlTestBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UrlTestBuilder expiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public UrlTestBuilder clickCount(Long clickCount) {
        this.clickCount = clickCount;
        return this;
    }

    public UrlTestBuilder active(Boolean active) {
        this.active = active;
        return this;
    }

    public Url build() {
        return new Url(
                id,
                originalUrl,
                shortCode,
                createdAt,
                expiresAt,
                clickCount,
                active
        );
    }
}