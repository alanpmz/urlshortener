package com.alanpmz.urlshortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateUrlRequest(@Future(message = "expiry date need to be in the future") LocalDateTime expiresAt,
                               Boolean active) {
}
