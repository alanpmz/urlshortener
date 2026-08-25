package com.alanpmz.urlshortener.dto;

import com.alanpmz.urlshortener.validation.ValidUrl;
import jakarta.validation.constraints.NotBlank;

public record CreateUrlRequest(@NotBlank @ValidUrl String url) {
}
